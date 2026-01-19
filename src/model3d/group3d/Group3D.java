package model3d.group3d;
/**
 * Colección de objetos que se manejan de forma conjunta
 * 
 * @author MAZ
 */
import java.util.Collection;
import org.jogamp.vecmath.Point3f;
//
import static raytracer.Hit.NOHIT;
import raytracer.Hit;
import raytracer.Ray;
import model3d.Model3D;
import model3d.VolumetricIntersectable;
//
public class Group3D implements VolumetricIntersectable {
  
  private final Collection<Model3D> objects;
  
  public Group3D (final Collection<Model3D> objects) {
    this.objects = objects;
  }

  @Override
  public Hit intersect (final Ray ray, final float tmin, final float tmax) {
    
    Hit closestHit = NOHIT;

    for (final Model3D objeto: objects) {
      
      final Hit lastHit = objeto.intersect(ray, tmin, tmax);

      if (lastHit.isCloserThan(closestHit)) {
        lastHit.setModel3D(objeto);
        closestHit = lastHit;
      }

    }

    return closestHit;
    
  }

  @Override
  public boolean intersectAny (final Ray ray, final float tmin, final float tmax) {

    final float shift = Ray.SHIFT_DISTANCE;

    return objects.stream()
              .map((x) -> x.intersect(ray))
              .filter((x) -> x != NOHIT)
              .anyMatch((x) -> (Math.signum(x.getAlpha() + shift - tmax) < 0));
    
  }

  @Override
  public boolean isInside (final Point3f P) {
    return objects.stream().anyMatch((x) -> x.isInside(P));
  }

  @Override
  public boolean isOutside (final Point3f P) {
    return objects.stream().allMatch((x) -> x.isInside(P));
  }

  @Override
  public Hit intersectFromOutside (final Ray ray) {
    return intersectFromOutside(ray, 0, Float.POSITIVE_INFINITY);
  }

  @Override
  public Hit intersectFromInside (final Ray ray) {
    return intersectFromInside(ray, 0, Float.POSITIVE_INFINITY);
  }

  @Override
  public Hit intersectFromOutside (final Ray ray, final float tmin, final float tmax) {
    
    Hit closestHit = NOHIT;

    for (final Model3D objeto: objects) {
      
      final Hit lastHit = objeto.intersect(ray, tmin, tmax);

      if (lastHit.isCloserThan(closestHit)) {
        lastHit.setModel3D(objeto);
        closestHit = lastHit;
      }

    }

    return closestHit;
    
  }

  @Override
  public Hit intersectFromInside (final Ray ray, final float tmin, final float tmax) {
    
    Hit closestHit = NOHIT;

    for (final Model3D objeto: objects) {
      
      final Hit lastHit = objeto.intersect(ray, tmin, tmax);

      if (lastHit.isCloserThan(closestHit)) {
        lastHit.setModel3D(objeto);
        closestHit = lastHit;
      }

    }

    return closestHit;

  }

}