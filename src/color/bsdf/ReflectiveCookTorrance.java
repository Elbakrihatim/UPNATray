package color.bsdf;
/**
 *
 * @author MAZ
 */
import static engine.RadianceRGB.NORADIANCE;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.fma;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import org.jogamp.vecmath.Vector3f;
//
import static primitives.ExtendedOperators.cdot;
import color.BRDF;
import color.densityfunction.NormalDistributionFunction;
import color.reflectance.ReflectanceRGB;
import engine.RadianceRGB;
//
public final class ReflectiveCookTorrance extends BRDF {
  
  public ReflectiveCookTorrance (final ReflectanceRGB diffuse,
                                 final ReflectanceRGB specular,
                                 final NormalDistributionFunction ndf) {
    super(diffuse, specular, ndf);  
  }

  @Override
  protected RadianceRGB diffuseFilter (final RadianceRGB inputRadiance,
                                       final Vector3f wi,
                                       final Vector3f wo,
                                       final Vector3f n,
                                       final float cosi) {
    return NORADIANCE;
  }  

  @Override
  protected RadianceRGB reflectiveFilter (final RadianceRGB inputRadiance,
                                          final Vector3f wi,
                                          final Vector3f wo,
                                          final Vector3f n,
                                          final float cosi) {
    return NORADIANCE;
  }

}