package model3d;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Point3f;
//
import static raytracer.Hit.NOHIT;
import raytracer.Hit;
import raytracer.Ray;
//
public interface VolumetricIntersectable extends Intersectable, Volumetric {
  
  @Override
  public default Hit intersect (final Ray ray) {
    final Point3f R = ray.getStartingPoint();
    if (isOutside(R))
      return intersectFromOutside(ray);
    else if (isInside(R))
      return intersectFromInside(ray);
    else
      return NOHIT;
  }
  
  @Override
  public default Hit intersect (final Ray ray, final float tmin, final float tmax) {
    final Point3f R = ray.getStartingPoint();
    if (isOutside(R))
      return intersectFromOutside(ray, tmin, tmax);
    else if (isInside(R))
      return intersectFromInside(ray, tmin, tmax);
    else
      return NOHIT;
  }  
  
  public Hit intersectFromOutside (final Ray ray, final float tmin, final float tmax);
  public default Hit intersectFromOutside (final Ray ray) {
    return intersectFromOutside(ray, 0, Float.POSITIVE_INFINITY);
  }
  
  public Hit intersectFromInside (final Ray ray, final float tmin, final float tmax);
  public default Hit intersectFromInside (final Ray ray) {
    return intersectFromInside(ray, 0, Float.POSITIVE_INFINITY);
  }
  
}