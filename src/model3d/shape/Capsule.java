package model3d.shape;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.abs;
import static java.lang.Math.fma;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import model3d.boundingvolume.AABB;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import static raytracer.Hit.NOHIT;
import raytracer.Hit;
import raytracer.Ray;
//
public final class Capsule extends ProceduralGeometry3D {
 
  private final Point3f A;
  private final Point3f B;
  private final Vector3f axis; // B - A
  private final float L;
  private final float r;
  private final float inv_r;

  public Capsule (final Point3f A, final Point3f B, final float r) {
    
    if (signum(r) <= 0)
      throw new IllegalArgumentException("Valor de radio illegal: " + r);
    
    this.A = new Point3f(A);
    this.B = new Point3f(B);    
    this.axis = new Vector3f();
    this.axis.sub(B, A); // B - A
    this.axis.normalize();
    
    this.L = A.distance(B);
    this.r = r;
    this.inv_r = 1.0f / r;

    final float Xmin = min(A.x - r, B.x - r);
    final float Xmax = max(A.x + r, B.x + r);
    final float Ymin = min(A.y - r, B.y - r);
    final float Ymax = max(A.y + r, B.y + r);  
    final float Zmin = min(A.z - r, B.z - r);
    final float Zmax = max(A.z + r, B.z + r);
    
    this.boundingBox = new AABB(Xmin, Xmax, Ymin, Ymax, Zmin, Zmax);

  }  
  
  @Override
  protected float SDF (final Point3f P) {
    return Float.POSITIVE_INFINITY;
  }

  @Override
  public Hit intersectFromOutside (final Ray ray) {
    if (signum(SDF(ray.getStartingPoint())) > 0)
      return intersect(true, ray);
    else
      return Hit.NOHIT;
  }

  @Override
  public Hit intersectFromInside (final Ray ray) {
    if (signum(SDF(ray.getStartingPoint())) < 0)
      return intersect(false, ray);
    else
      return Hit.NOHIT;    
  }  

  @Override
  public Hit intersect (final boolean fromOutside, final Ray ray,
                        final float tmin, final float tmax) {

    return NOHIT;
    
  }
  
  @Override
  protected float distanceUpperBound (final Point3f P) {
    return (float) (max(A.distance(P), B.distance(P)) + r);
  }  

  @Override
  public AABB getBoundingBox () {
    return boundingBox;
  }

  @Override
  public boolean intersectAny(Ray ray, float tmin, float tmax) {
    final Hit hit = intersect(ray, tmin, tmax);
    return hit.hits();
  }
  
}