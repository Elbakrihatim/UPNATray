package model3d;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Point3f;
//
public interface Volumetric {
  
  public boolean isInside  (final Point3f P);
  public boolean isOutside (final Point3f P);
  
}
