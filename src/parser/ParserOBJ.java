package parser;
/**
 *
 * @author MAZ
 */
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipInputStream;
import model3d.facet.BilinearPatch;
import org.jogamp.vecmath.TexCoord2f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import model3d.facet.Facet;
import model3d.facet.Triangle;
import model3d.facet.Vertex3D;
import model3d.shape.PolygonalMesh;
import model3d.kdtree.KdTree;
//
public final class ParserOBJ  {
    
  // Máximo número de facetas que puede haber en una hoja del KdTree
  static private final int MAX_POPULATION_LEAF =
    Integer.parseInt(System.getProperty("MAX_POPULATION_LEAF"));    
  
  final Set<Facet> facets = new LinkedHashSet<>();
  final Map<Integer, Vertex3D> vertex = new LinkedHashMap<>();
  final Map<Integer, Vector3f> normals = new LinkedHashMap<>();
  final Map<Integer, TexCoord2f> uv = new LinkedHashMap<>();
  final Map<Vertex3D, Vector3f> vertexToNormal = new IdentityHashMap<>();

  public PolygonalMesh parse (final File objFile,
                              final boolean flat,
                              final boolean promediatedNormals) {

    final String filePath;
    try {
      filePath = objFile.getCanonicalPath();
    } catch (final IOException ex) {
      System.err.println("Problema de I/O con fichero " + objFile);
      return new PolygonalMesh(facets, new IdentityHashMap<>());
    }

    try {

      final String fileName = objFile.getName();
      if (fileName.endsWith(".zip")) {

        try (final FileInputStream fis = new FileInputStream(objFile);
             final BufferedInputStream bis = new BufferedInputStream(fis);
             final ZipInputStream zis = new ZipInputStream(bis)) {

          if (zis.getNextEntry() != null) {
            try (final InputStreamReader ir = new InputStreamReader(zis);
                 final BufferedReader is = new BufferedReader(ir);) {
              return parse(is, flat, promediatedNormals);
            }
          }

        }

      } else {
        try (final FileReader fr = new FileReader(objFile);
             final BufferedReader is = new BufferedReader(fr)) {
          return parse(is, flat, promediatedNormals);
        }
      }

    } catch (final FileNotFoundException ex) {
      System.err.println("Fichero " + filePath + " no encontrado");
    } catch (final IOException ex) {
      System.err.println("Problema de I/O con fichero " + filePath);
    }

    return new PolygonalMesh(facets, new IdentityHashMap<>());

  }

  private PolygonalMesh parse (final Reader is,
                               final boolean flat,
                               final boolean promediatedNormals) {

    try (final Scanner scanner = new Scanner(is)) {

      scanner.useLocale(new Locale("en"));

      int nVertices = 0;
      int nNormals  = 0;
      int nTextures = 0;

      while (scanner.hasNextLine()) {

        if (scanner.hasNext()) {

          final String type = scanner.next();
          switch(type) {

            case "v": {
              final float x = scanner.nextFloat();
              final float y = scanner.nextFloat();
              final float z = scanner.nextFloat();
              final Point3f P = new Point3f(x, y, z);
              final Vertex3D V = new Vertex3D(P);
              vertex.put(++nVertices, V);
              // Se instancia vector para calcular normal promediada (si necesario).
              vertexToNormal.put(V, new Vector3f());
            }
            break;

            case "vn": {
              final float x = scanner.nextFloat();
              final float y = scanner.nextFloat();
              final float z = scanner.nextFloat();
              normals.put(++nNormals, new Vector3f(x, y, z));
            }
            break;

            case "vt": {
              final float u = scanner.nextFloat();
              final float v = scanner.nextFloat();
              uv.put(++nTextures, new TexCoord2f(u, v));
            }
            break;

            case "f": {

              final ArrayList<String> _groups = new ArrayList<>();
              do {
                _groups.add(scanner.next());
              } while (scanner.hasNext("(\\d+)|(\\d+/\\d+)|(\\d+/\\d*/\\d*)"));
              
              final var groups = _groups.toArray(String[]::new);
              

              switch (groups.length) {
                case 3:
                  {
                    final Facet f = getTriangle(groups, promediatedNormals, flat);
                    facets.add(f);
                    break;
                  }
                case 4:
                  {
                    final Facet f = getQuadrangle(groups, promediatedNormals, flat);
                    facets.add(f);
                    break;
                  }
                default:
                  getPolygon(facets, groups, promediatedNormals, flat);
                  break; 
              }

            }
            break;

          }

        } else
          scanner.nextLine();

      }

      if (normals.isEmpty() || promediatedNormals) {
        // Modelo OBJ no incluye normales, o se quieren emplear promediadas.

        // Se asocia una normal promedio a cada vértice del triángulo.
        for (final Vertex3D V: vertexToNormal.keySet()) {
          final Vector3f n = vertexToNormal.get(V);
          n.normalize();
          V.setNormal(n);
        }

      }

      System.out.println("Vértices: " + nVertices);
      System.out.println("Normales: " + nNormals);
      System.out.println("Coordenadas de textura: " + nTextures);
      System.out.println("Facetas: " + facets.size());
      
      final PolygonalMesh mesh =
        new PolygonalMesh(facets, getMap(vertexToNormal.keySet(), facets));
      mesh.set(new KdTree(MAX_POPULATION_LEAF));
      
      return mesh;

    }

  }
  
  private Facet getTriangle (final String[] groups,
                             final boolean promediatedNormals,
                             final boolean flat) {
    
    // Elementos de definición
    final String[] stA = groups[0].split("/");
    final String[] stB = groups[1].split("/");
    final String[] stC = groups[2].split("/");

    // Vértices
    final Vertex3D A = vertex.get(Integer.parseInt(stA[0]));
    final Vertex3D B = vertex.get(Integer.parseInt(stB[0]));
    final Vertex3D C = vertex.get(Integer.parseInt(stC[0]));
    
    // Coordenadas de textura
    if ((stA.length > 1) && !stA[1].isEmpty()) {
      final int uvA = Integer.parseInt(stA[1]);
      A.setTextureCoordenates(uv.get(uvA));
      final int uvB = Integer.parseInt(stB[1]);
      B.setTextureCoordenates(uv.get(uvB));
      final int uvC = Integer.parseInt(stC[1]);
      C.setTextureCoordenates(uv.get(uvC));
    }
    
    // Sin normales; se calculan promediadas.
    if ((stA.length < 3) || promediatedNormals) {

      final Vector3f AB = new Vector3f();
      AB.sub(B.getPoint(), A.getPoint()); // B - A
      final Vector3f AC = new Vector3f();
      AC.sub(C.getPoint(), A.getPoint()); // C - A

      final Vector3f normal = new Vector3f();
      normal.cross(AB, AC);
      final Vector3f nA = vertexToNormal.get(A);
      nA.add(normal);
      final Vector3f nB = vertexToNormal.get(B);
      nB.add(normal);
      final Vector3f nC = vertexToNormal.get(C);
      nC.add(normal);

    }
    
    // Con normales
    if ((stA.length > 2) && !stA[2].isEmpty() && !promediatedNormals) {

      final Vector3f nA = normals.get(Integer.parseInt(stA[2]));
      final Vector3f nB = normals.get(Integer.parseInt(stB[2]));
      final Vector3f nC = normals.get(Integer.parseInt(stC[2]));

      A.setNormal(nA);
      B.setNormal(nB);
      C.setNormal(nC);
    }
    
    final Facet f = new Triangle(A, B, C);
    if (flat)
      f.setFlat();
    
    return f;    

  }
  
  private Facet getQuadrangle (final String[] groups,
                               final boolean promediatedNormals, final boolean flat) {
      
    // Elementos de definición
    final String[] stA = groups[0].split("/");
    final String[] stB = groups[1].split("/");
    final String[] stC = groups[2].split("/");
    final String[] stD = groups[3].split("/");    

    // Vértices
    final Vertex3D A = vertex.get(Integer.parseInt(stA[0]));
    final Vertex3D B = vertex.get(Integer.parseInt(stB[0]));
    final Vertex3D C = vertex.get(Integer.parseInt(stC[0]));
    final Vertex3D D = vertex.get(Integer.parseInt(stD[0]));    
    
    // Coordenadas de textura
    if ((stA.length > 1) && !stA[1].isEmpty()) {
      final int uvA = Integer.parseInt(stA[1]);
      A.setTextureCoordenates(uv.get(uvA));
      final int uvB = Integer.parseInt(stB[1]);
      B.setTextureCoordenates(uv.get(uvB));
      final int uvC = Integer.parseInt(stC[1]);
      C.setTextureCoordenates(uv.get(uvC));
      final int uvD = Integer.parseInt(stD[1]);
      D.setTextureCoordenates(uv.get(uvD));      
    }
    
    // Sin normales; se calculan promediadas.
    if ((stA.length < 3) || promediatedNormals) {

      // Normales geométricas      
      final Vector3f AB = new Vector3f();
      AB.sub(B.getPoint(), A.getPoint()); // B - A
      final Vector3f AC = new Vector3f();
      AC.sub(C.getPoint(), A.getPoint()); // C - A
      final Vector3f AD = new Vector3f();
      AD.sub(D.getPoint(), A.getPoint()); // D - A
      final Vector3f DB = new Vector3f();
      DB.sub(B.getPoint(), D.getPoint()); // B - D
      final Vector3f DC = new Vector3f();
      DC.sub(C.getPoint(), D.getPoint()); // C - D                    

      final Vector3f n = new Vector3f();

      n.cross(AB, AD);
      final Vector3f nA = vertexToNormal.get(A);
      nA.add(n);

      //n.cross(AC, AD);
      n.cross(DC, AD);
      final Vector3f nD = vertexToNormal.get(D);
      nD.add(n);

      n.cross(AB, AC);
      final Vector3f nB = vertexToNormal.get(B);
      nB.add(n);

      n.cross(DB, DC);
      final Vector3f nC = vertexToNormal.get(C);
      nC.add(n);

    }
    
    // Con normales
    if ((stA.length > 2) && !stA[2].isEmpty() && !promediatedNormals) {

      final Vector3f nA = normals.get(Integer.parseInt(stA[2]));
      final Vector3f nB = normals.get(Integer.parseInt(stB[2]));
      final Vector3f nC = normals.get(Integer.parseInt(stC[2]));
      final Vector3f nD = normals.get(Integer.parseInt(stD[2]));      

      A.setNormal(nA);
      B.setNormal(nB);
      C.setNormal(nC);
      D.setNormal(nD);
      
    }
    
    final Facet f = new BilinearPatch(A, B, C, D);
    if (flat)
      f.setFlat();
    
    return f;

  }
 
  private void getPolygon (final Collection<Facet> facets, final String[] groups,
                           final boolean promediatedNormals, final boolean flat) {

    final int s = groups.length;
    
  }
  
  private Map<Vertex3D, Set<Facet>> getMap (final Collection<Vertex3D> vertex,
                                            final Set<Facet> facets) {

    final Map<Vertex3D, Set<Facet>> vertexToFacetMap = new IdentityHashMap<>();
    for (final Vertex3D v: vertex) {
      vertexToFacetMap.put(v, new LinkedHashSet());
    }
    for (final Facet f: facets) {
      for (final Vertex3D V: f.getVertices()) {
        final Set<Facet> _facets = vertexToFacetMap.get(V);
        if (_facets.contains(f))
          throw new IllegalArgumentException("Inconsistencia en malla: cara duplicada");
        _facets.add(f);
      }
    }
    return vertexToFacetMap;
  }

}