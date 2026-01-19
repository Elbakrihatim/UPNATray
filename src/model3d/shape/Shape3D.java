package model3d.shape;
/**
 *
 * @author MAZ
 */
import model3d.VolumetricIntersectable;
import static model3d.boundingvolume.AABB.NOBOUNDINGBOX;
import static raytracer.Hit.NOHIT;
import model3d.boundingvolume.AABB;
import raytracer.Hit;
import raytracer.Ray;
//
public interface Shape3D extends VolumetricIntersectable {
  
  public Hit intersect (final boolean fromOutside, final Ray ray, final float tmin, final float tmax);  

  default public Hit intersect (final boolean fromOutside, final Ray ray) {
    return intersect(fromOutside, ray, 0, Float.POSITIVE_INFINITY);
  }

  @Override
  default public Hit intersect (final Ray ray, final float tmin, final float tmax) { 
    if (isOutside(ray.getStartingPoint()))
      return intersect(true, ray, tmin, tmax);
    else if (isInside(ray.getStartingPoint()))
      return intersect(false, ray, tmin, tmax);
    else
      return NOHIT;
  }
  
  @Override
  default public Hit intersect (final Ray ray) { 
    if (isOutside(ray.getStartingPoint()))
      return intersect(true, ray, 0, Float.POSITIVE_INFINITY);
    else if (isInside(ray.getStartingPoint()))
      return intersect(false, ray, 0, Float.POSITIVE_INFINITY);
    else
      return NOHIT;
  }
  
  @Override
  default public Hit intersectFromOutside (final Ray ray,
                                           final float tmin, final float tmax) {
    return isOutside(ray.getStartingPoint()) ?
                     intersect(true, ray, tmin, tmax) : NOHIT;
  }  
  
  @Override
  default public Hit intersectFromOutside (final Ray ray) {
    return isOutside(ray.getStartingPoint()) ?
                     intersect(true, ray, 0, Float.POSITIVE_INFINITY) : NOHIT;
  }
 
  @Override
  default public Hit intersectFromInside  (final Ray ray,
                                           final float tmin, final float tmax) {
    return isInside(ray.getStartingPoint()) ?
                    intersect(false, ray, tmin, tmax) : NOHIT;
  }  
  
  @Override
  default public Hit intersectFromInside  (final Ray ray) {
    return isInside(ray.getStartingPoint()) ?
                    intersect(false, ray, 0, Float.POSITIVE_INFINITY) : NOHIT;
  }  
  
  default public AABB getBoundingBox () {
    return NOBOUNDINGBOX;
  }
  
}