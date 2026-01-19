package model3d.boundingvolume;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.abs;
import static java.lang.Math.signum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import model3d.facet.Vertex3D;
import org.jogamp.vecmath.Matrix4f;
import raytracer.Ray;
//
public class AABB implements BoundingVolume {

  static public final AABB NOBOUNDINGBOX = new NoBoundingBox();

  public final float Xmax;
  public final float Ymax;
  public final float Zmax;
  public final float Xmin;
  public final float Ymin;
  public final float Zmin;

  protected final float halfLx;
  protected final float halfLy;
  protected final float halfLz;

  protected final float Cx;
  protected final float Cy;
  protected final float Cz;
  
  private static Set<Point3f> getPoints (final Set<Vertex3D> vertices) {
    final Set<Point3f> points = new LinkedHashSet<>();
    for (final Vertex3D v: vertices)
      points.add(v.getPoint());
    return points;
  }

  public AABB (final float xmin, final float xmax,
               final float ymin, final float ymax,
               final float zmin, final float zmax) {
      
    Xmax = xmax;
    Xmin = xmin;
    Ymax = ymax;
    Ymin = ymin;
    Zmax = zmax;
    Zmin = zmin;

    halfLx = (Xmax - Xmin) * 0.5f;
    halfLy = (Ymax - Ymin) * 0.5f;
    halfLz = (Zmax - Zmin) * 0.5f;

    Cx = (Xmax + Xmin) * 0.5f;
    Cy = (Ymax + Ymin) * 0.5f;
    Cz = (Zmax + Zmin) * 0.5f;

  }
  
  public AABB (final Set<Vertex3D> vertices) {
    this(getPoints(vertices));
  }  

  public AABB (final Collection<Point3f> points) {

    float Xmax_ = Float.NEGATIVE_INFINITY;
    float Ymax_ = Float.NEGATIVE_INFINITY;
    float Zmax_ = Float.NEGATIVE_INFINITY;
    float Xmin_ = Float.POSITIVE_INFINITY;
    float Ymin_ = Float.POSITIVE_INFINITY;
    float Zmin_ = Float.POSITIVE_INFINITY;
    
    for (final Point3f P: points) {
      if (Math.signum(P.x - Xmax_) > 0) {
        Xmax_ = P.x;
      }
      if (Math.signum(P.x - Xmin_) < 0) {
        Xmin_ = P.x;
      }
      if (Math.signum(P.y - Ymax_) > 0) {
        Ymax_ = P.y;
      }
      if (Math.signum(P.y - Ymin_) < 0) {
        Ymin_ = P.y;
      }
      if (Math.signum(P.z - Zmax_) > 0) {
        Zmax_ = P.z;
      }
      if (Math.signum(P.z - Zmin_) < 0) {
        Zmin_ = P.z;
      }
    }
    Xmax = Xmax_;
    Xmin = Xmin_;
    Ymax = Ymax_;
    Ymin = Ymin_;
    Zmax = Zmax_;
    Zmin = Zmin_;

    halfLx = (Xmax - Xmin) * 0.5f;
    halfLy = (Ymax - Ymin) * 0.5f;
    halfLz = (Zmax - Zmin) * 0.5f;

    Cx = (Xmax + Xmin) * 0.5f;
    Cy = (Ymax + Ymin) * 0.5f;
    Cz = (Zmax + Zmin) * 0.5f;

  }

  @Override
  public boolean isInside (final Point3f P) {
    // P está dentro de la caja orientada con los ejes si, en cada dimensión,
    // el valor absoluto de la diferencia entre las correspondiente coordenadas
    // de P y el punto central C es menor que la mitad de la longitud de la caja
    // en esa dimensión.
    return abs(P.x - Cx) <= halfLx &&
           abs(P.y - Cy) <= halfLy &&
           abs(P.z - Cz) <= halfLz;
  }

  @Override
  public boolean isOutside (final Point3f P) {
    // P está fuera de la caja orientada con los ejes si, en elguna dimensión,
    // el valor absoluto de la diferencia entre las correspondiente coordenadas
    // de P y el punto central C es mayor que la mitad de la longitud de la caja
    // en esa dimensión.  
    return abs(P.x - Cx) > halfLx ||
           abs(P.y - Cy) > halfLy ||
           abs(P.z - Cz) > halfLz;
  }

  @Override
  public boolean intersect (final Ray ray, final float tmin, final float tmax) {
    float tNear = tmin;
    float tFar = tmax;
    Point3f origin = ray.getStartingPoint();
    Vector3f direction = ray.getDirection();
    // Verificar intersecciones para cada eje
    for (int i = 0; i < 3; i++) {
        float rayOrigin = (i == 0) ? origin.x : (i == 1) ? origin.y : origin.z;
        float rayDir = (i == 0) ? direction.x : (i == 1) ? direction.y : direction.z;
        float minBound = (i == 0) ? Xmin : (i == 1) ? Ymin : Zmin;
        float maxBound = (i == 0) ? Xmax : (i == 1) ? Ymax : Zmax;
        // Calcular los valores t para las intersecciones con los planos del eje actual
        float invDir = 1.0f / rayDir;
        float t0 = (minBound - rayOrigin) * invDir;
        float t1 = (maxBound - rayOrigin) * invDir;
        // Asegurar que t0 sea el más cercano y t1 el más lejano
        if (t0 > t1) { // debemos asegurar que t0 sea la entrada y t1 la salida.
            float temp = t0;
            t0 = t1;
            t1 = temp;
        }
        // Actualizar tNear y tFar
        tNear = Math.max(tNear, t0); // el mayot de los t0 calculados para cada eje
        tFar = Math.min(tFar, t1);  // el menor de los t1 calculados para cada eje
        // Si los intervalos no se solapan, no hay intersección
        if (tNear > tFar) { // no hay intersección
            return false;
        }
    }
    return true; // hay intersección
  }

  @Override
  public float[] intersectComplete (final Ray ray) {
      float tNear = Float.NEGATIVE_INFINITY;
    float tFar = Float.POSITIVE_INFINITY;
    Point3f origin = ray.getStartingPoint();
    Vector3f direction = ray.getDirection();
    for (int i = 0; i < 3; i++) {
        float rayOrigin = (i == 0) ? origin.x : (i == 1) ? origin.y : origin.z;
        float rayDir = (i == 0) ? direction.x : (i == 1) ? direction.y : direction.z;
        float minBound = (i == 0) ? Xmin : (i == 1) ? Ymin : Zmin;
        float maxBound = (i == 0) ? Xmax : (i == 1) ? Ymax : Zmax;
        float invDir = 1.0f / rayDir;
        float t0 = (minBound - rayOrigin) * invDir;
        float t1 = (maxBound - rayOrigin) * invDir;
        if (t0 > t1) {
            float temp = t0;
            t0 = t1;
            t1 = temp;
        }
        tNear = Math.max(tNear, t0);
        tFar = Math.min(tFar, t1);
        if (tNear > tFar) {
            return new float[0];
        }
    }
    return new float[]{tNear, tFar};
  }

  public Collection<Point3f> getCorners () {

    // Esquinas del ortoedro
    final Collection<Point3f> corners = new ArrayList<>();
    corners.add(new Point3f(Xmin, Ymin, Zmin));
    corners.add(new Point3f(Xmax, Ymin, Zmin));
    corners.add(new Point3f(Xmin, Ymax, Zmin));
    corners.add(new Point3f(Xmax, Ymax, Zmin));
    corners.add(new Point3f(Xmin, Ymin, Zmax));
    corners.add(new Point3f(Xmax, Ymin, Zmax));
    corners.add(new Point3f(Xmin, Ymax, Zmax));
    corners.add(new Point3f(Xmax, Ymax, Zmax));

    return corners;

  }
  
  public AABB transformedBy (final Matrix4f M) {
        
     // Vértices (esquinas) del ortoedro
    final Collection<Point3f> corners = getCorners();

    // Transformación de vértices
    for (final Point3f P: corners)
      M.transform(P);

    // Nueva AABB
    return new AABB(corners);
      
  }

  @Override
  public String toString () {
    String s = "Xmin: " + Xmin + " Xmax: " + Xmax;
    s += " Ymin: " + Ymin + " Ymax: " + Ymax;
    s += " Zmin: " + Zmin + " Zmax: " + Zmax;
    return s;
  }

}