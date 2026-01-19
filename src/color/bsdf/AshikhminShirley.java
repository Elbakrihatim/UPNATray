package color.bsdf;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Vector3f;
//
import color.BRDF;
import color.densityfunction.NormalDistributionFunction;
import color.reflectance.ReflectanceRGB;
import engine.RadianceRGB;
import static engine.RadianceRGB.NORADIANCE;
import static java.lang.Math.abs;
import static java.lang.Math.min;
//
public final class AshikhminShirley extends BRDF {

  private static final double DIFFUSE_CORRECTION_FACTOR = (28.0 / 23.0);

  public AshikhminShirley (final ReflectanceRGB diffuse,
                           final ReflectanceRGB specular,
                           final NormalDistributionFunction ndf) {
    super(new ReflectanceRGB(diffuse.getR() * (1 - specular.getR()),
                             diffuse.getG() * (1 - specular.getG()), 
                             diffuse.getB() * (1 - specular.getB())), specular, ndf);
  } 
  
  @Override
  protected RadianceRGB diffuseFilter (final RadianceRGB inputRadiance,
                                       final Vector3f in,
                                       final Vector3f v,
                                       final Vector3f n,
                                       final float cosi) {

    return NORADIANCE;
    
  }
    
  @Override
  protected RadianceRGB reflectiveFilter (final RadianceRGB inputRadiance,
                                          final Vector3f in,
                                          final Vector3f v,
                                          final Vector3f n,
                                          final float cosi) {
     
    return NORADIANCE;
    
  }
  
}