package model3d.shape;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.abs;
import static java.lang.Math.fma;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import static raytracer.Hit.NOHIT;
import model3d.boundingvolume.AABB;
import raytracer.Hit;
import raytracer.Ray;
//
public final class Genus2 extends ProceduralGeometry3D {
  
  static private final Point3f O = new Point3f(0, 0, 0);
  static private final float D = Float.POSITIVE_INFINITY;
  
  private float nx, ny, nz;
  
  public Genus2 () {
     this.boundingBox = new AABB(-D, +D, -D, +D, -D, +D);
  } 
  
  @Override
  protected float SDF (final Point3f P) {

    return Float.POSITIVE_INFINITY;

  }
  
  @Override
  public Hit intersectFromOutside(Ray ray) {
    if (signum(SDF(ray.getStartingPoint())) > 0)
      return intersect(true, ray);
    else
      return Hit.NOHIT;
  }

  @Override
  public Hit intersectFromInside(Ray ray) {
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
    return P.distance(O) + D;
  }
  
  @Override
  public AABB getBoundingBox () {
    return boundingBox;
  }

  @Override
  public boolean intersectAny (final Ray ray, final float tmin, final float tmax) {
    final Hit hit = intersect(ray);
    final float a = hit.getAlpha();
    return (signum(tmin - a) < 0) && (signum(a - tmax) <= 0);
  }
  
}