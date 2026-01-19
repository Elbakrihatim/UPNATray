package color.bsdf;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.abs;
import static java.lang.Math.fma;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import org.jogamp.vecmath.Vector3f;
//
import static engine.RadianceRGB.NORADIANCE;
import static primitives.ExtendedOperators.cdot;
import static primitives.ExtendedOperators.opposite;
import color.BTDF;
import color.densityfunction.NormalDistributionFunction;
import color.reflectance.ReflectanceRGB;
import engine.RadianceRGB;
//
public final class TransmissiveCookTorrance extends BTDF {
  
  public TransmissiveCookTorrance (final ReflectanceRGB specular,
                                   final float eta_int, final float eta_ext,
                                   final NormalDistributionFunction ndf) {
    super(specular, eta_int, eta_ext, ndf);  
  }
  
  @Override
  protected RadianceRGB reflectiveFilter (final boolean toOutside,
                                          final RadianceRGB inputRadiance,
                                          final Vector3f wi,                                         
                                          final Vector3f wo,                                    
                                          final Vector3f n,
                                          final float cosi) {
    return NORADIANCE;
  }
  
  @Override
  protected RadianceRGB transmissiveFilter (final boolean toOutside,
                                            final RadianceRGB inputRadiance,
                                            final Vector3f wi,                                        
                                            final Vector3f wo,
                                            final Vector3f m,
                                            final float cosi) {
    return NORADIANCE;
  }
  
}