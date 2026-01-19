package color.reflectance;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.fma;
import static java.lang.Math.min;
//
import engine.RadianceRGB;
//
public class ConductiveReflectance extends FresnelianReflectance {

  public ConductiveReflectance (final ReflectanceRGB r) {
    super(r);
  }
  
  public RadianceRGB reflectiveFilter (final RadianceRGB radiance) {
    return super.filter(radiance);
  }  

  public RadianceRGB reflectiveFilter (final float cosi, final RadianceRGB radiance) {
    final float ct1 = 1.0f - min(1.0f, cosi);
    final float ct2 = ct1 * ct1;
    final float ct4 = ct2 * ct2;
    final float ct5 = ct4 * ct1;
    final float r = min(1.0f, fma(1 - xr, ct5, xr)) * radiance.r;
    final float g = min(1.0f, fma(1 - xg, ct5, xg)) * radiance.g;
    final float b = min(1.0f, fma(1 - xb, ct5, xb)) * radiance.b;
    return new RadianceRGB(r, g, b);
  } 
  
  @Override
  public boolean isDielectric () {
    return false;
  }

  @Override
  public boolean isConductive () {
    return true;
  }
  
}
