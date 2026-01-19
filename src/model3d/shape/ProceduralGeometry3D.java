package model3d.shape;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.abs;
import static java.lang.Math.fma;
import static java.lang.Math.signum;
import org.jogamp.vecmath.Point3f;
//
import model3d.boundingvolume.AABB;
import raytracer.Ray;
//
public abstract class ProceduralGeometry3D implements Shape3D {

  static private final float MIN_STEP_CONTRIBUTION = 5E-6f;
  
  protected AABB boundingBox = null;
  
  protected final float rayMarching (final Ray ray) {  
    
    final Point3f R = ray.getStartingPoint();
    
    float d = 0.0f;
    final float distanceUpperBound = distanceUpperBound(R);

    for (float step = SDF(R); (signum(fma(MIN_STEP_CONTRIBUTION, abs(d), -abs(step))) <= 0)
                               &&
                              (signum(abs(d) - distanceUpperBound) < 0);) {
      
      d += step;
      step = SDF(ray.pointAtParameter(abs(d)));
      if (signum(abs(step) - MIN_STEP_CONTRIBUTION) < 0)
        break;     
      
    }
    
    if (signum(abs(d) - distanceUpperBound) < 0) {
      return d;
    } else {
      return Float.POSITIVE_INFINITY;
    }

  }
 
  protected abstract float SDF (final Point3f P);
  
  protected abstract float distanceUpperBound (final Point3f P);
  
  @Override
  public boolean isInside (final Point3f P) {
    return signum(SDF(P)) < 0;
  }

  @Override
  public boolean isOutside (final Point3f P) {
    return signum(SDF(P)) > 0;
  }
    
}