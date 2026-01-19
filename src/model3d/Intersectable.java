package model3d;
/**
 *
 * @author MAZ
 */
import raytracer.Hit;
import raytracer.Ray;
//
public interface Intersectable {
  
  public Hit intersect (final Ray ray, final float tmin, final float tmax);

  default public Hit intersect (final Ray ray) {
    return intersect(ray, 0, Float.POSITIVE_INFINITY);
  }

  public boolean intersectAny (final Ray ray, final float tmin, final float tmax);  
  
  default public boolean intersectAny (final Ray ray) {
    return intersectAny(ray, 0, Float.POSITIVE_INFINITY);
  }
  
}
