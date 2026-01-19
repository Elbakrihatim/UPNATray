package model3d.kdtree;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.signum;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import org.jogamp.vecmath.Point3f;
//
import model3d.boundingvolume.AABB;
import model3d.facet.Facet;
import model3d.shape.PolygonalMesh;
import raytracer.Hit;
import raytracer.Ray;
//
public final class KdTree {
  
  public static enum AXIS { X, Y, Z }

  // Valores bajos mejoran rendimiento en ejecución del algoritmo de intersección
  // a costa de un mayor coste de construcción (en tiempo y en espacio) del árbol.
  static private final int LEAF_MAX_SIZE = 256;
  
  private float X_min_length;
  private float Y_min_length;
  private float Z_min_length;
  private Node root;
  private final int leaf_max_size;
  
  public KdTree (final int n) {
    if (n <= 0)
      throw new IllegalArgumentException("Valor para tamaño de hoja debe ser estrictamente positivo");
    this.leaf_max_size = n;
  }
  
  public KdTree () {
    this(LEAF_MAX_SIZE);
  }  

  public void set (final PolygonalMesh mesh) {
    final long t0 = System.nanoTime();      
    final AABB boundingBox = mesh.getBoundingBox();
    final Set<Facet> facets = mesh.getFacets();
    final double size3 = Math.pow(facets.size(), 0.85);
    final float Xmin = boundingBox.Xmin;
    final float Xmax = boundingBox.Xmax;
    final float Ymin = boundingBox.Ymin;
    final float Ymax = boundingBox.Ymax;
    final float Zmin = boundingBox.Zmin;
    final float Zmax = boundingBox.Zmax;  
    final float Xdelta = Xmax - Xmin;
    final float Ydelta = Ymax - Ymin;
    final float Zdelta = Zmax - Zmin;      
    X_min_length = (float) (Xdelta / size3);
    Y_min_length = (float) (Ydelta / size3);
    Z_min_length = (float) (Zdelta / size3);

    final AXIS axis;
    if ((signum(Ydelta - Xdelta) <= 0) && (signum(Zdelta - Xdelta) <= 0))
      axis = AXIS.X;
    else if ((signum(Xdelta - Ydelta) <= 0) && (signum(Zdelta - Ydelta) <= 0))
      axis = AXIS.Y;
    else //if ((signum(Xdelta - Zdelta) <= 0) && (signum(Ydelta - Zdelta) <= 0))
      axis = AXIS.Z;
    root = buildTree(facets, axis, Xmin, Xmax, Ymin, Ymax, Zmin, Zmax);
    final long t1 = System.nanoTime();
    System.out.printf("Tiempo para construcción del KsTree: %4.2f seconds\n", (float) (t1 - t0) * 1E-9);       
  }
  
  public boolean isInside (final Point3f P) {
    return root.isInside(P);
  }    

  public boolean isOutside (final Point3f P) {
    return root.isOutside(P);
  }
    
  public Hit intersectFromInside (final Ray ray, final float tmin, final float tmax) {
    return root.intersect(false, ray, tmin, tmax);
  }

  public Hit intersectFromOutside (final Ray ray, final float tmin, final float tmax) {
    return root.intersect(true, ray, tmin, tmax);
  }

  public boolean intersectAny (final Ray ray, final float tmin, final float tmax) {
    return root.intersectAny(ray, tmin, tmax);
  }
      
  private Node buildTree (final Set<Facet> facets, final AXIS axis,
                          final float Xmin, final float Xmax,
                          final float Ymin, final float Ymax,
                          final float Zmin, final float Zmax) {

    if ((facets.size() > leaf_max_size) && 
         (((axis == AXIS.X) && (signum(Xmax - Xmin - X_min_length) > 0)) || 
          ((axis == AXIS.Y) && (signum(Ymax - Ymin - Y_min_length) > 0)) ||
          ((axis == AXIS.Z) && (signum(Zmax - Zmin - Z_min_length) > 0)))) {

      final Node left;
      final Node right;
      final float splitValue;

      final Set<Facet> _anterior  = new LinkedHashSet<>();
      final Set<Facet> _posterior = new LinkedHashSet<>();
      final float splitValueFactor = 0.5f; // Como alternativa a la heurística SAH
      switch (axis) {
        
        case X:
          if (signum(Xmax - Xmin - X_min_length) <= 0)
            return buildTree(facets, AXIS.Y, Xmin, Xmax, Ymin, Ymax, Zmin, Zmax);
          splitValue = (Xmax + Xmin) * splitValueFactor;
          split(facets, splitValue, _anterior, _posterior, Facet::isXanterior, Facet::isXposterior);
          left  = buildTree(_posterior, AXIS.Y, Xmin, splitValue, Ymin, Ymax, Zmin, Zmax);
          right = buildTree(_anterior,  AXIS.Y, splitValue, Xmax, Ymin, Ymax, Zmin, Zmax);
          break;

        case Z:
          if (signum(Zmax - Zmin - Z_min_length) <= 0)
            return buildTree(facets, AXIS.X, Xmin, Xmax, Ymin, Ymax, Zmin, Zmax);
          splitValue = (Zmax + Zmin) * splitValueFactor;  
          split(facets, splitValue, _anterior, _posterior, Facet::isZanterior, Facet::isZposterior);
          left  = buildTree(_posterior, AXIS.X, Xmin, Xmax, Ymin, Ymax, Zmin, splitValue);
          right = buildTree(_anterior,  AXIS.X, Xmin, Xmax, Ymin, Ymax, splitValue, Zmax);
          break;

        case Y:
        default:
          if (signum(Ymax - Ymin - Y_min_length) <= 0)
            return buildTree(facets, AXIS.Z, Xmin, Xmax, Ymin, Ymax, Zmin, Zmax);
          splitValue = (Ymax + Ymin) * splitValueFactor;
          split(facets, splitValue, _anterior, _posterior, Facet::isYanterior, Facet::isYposterior);
          left  = buildTree(_posterior, AXIS.Z, Xmin, Xmax, Ymin, splitValue, Zmin, Zmax);
          right = buildTree(_anterior,  AXIS.Z, Xmin, Xmax, splitValue, Ymax, Zmin, Zmax);
          break;
          
      }

      return new InnerNode(axis, splitValue, left, right);

    } else if (!facets.isEmpty()) {
      return new Leaf(facets);
    } else
      return EmptyLeaf.EMPTY_LEAF;

  }
  
  private void split (final Set<Facet> facets,
                      final float splitValue,
                      final Set<Facet> anterior,
                      final Set<Facet> posterior,
                      final BiPredicate<Facet, Float> isAnterior,
                      final BiPredicate<Facet, Float> isPosterior) {
      

    facets.forEach((f) -> {
      if (isAnterior.test(f, splitValue))
        anterior.add(f);
      if (isPosterior.test(f, splitValue))
        posterior.add(f);
    });

  }
  
}