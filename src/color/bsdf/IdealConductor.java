package color.bsdf;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Vector3f;
//
import static engine.RadianceRGB.NORADIANCE;
import color.BRDF;
import color.reflectance.ReflectanceRGB;
import engine.RadianceRGB;
//
public final class IdealConductor extends BRDF {

  public IdealConductor (final ReflectanceRGB specular) {
    super(specular, null);
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
    // Solo refleja en la dirección de incidencia que la Ley de Snell
    // socia a los vectores wo (dirección de salida) y n (normal).
    // Si la dirección de incidencia wi no está sufcieintemente cerca
    // de la dirección _wi, se devuelve radiancia nula.
    final Vector3f _wi = super.getSpecularReflectionDirection(n, wo);
    if (_wi.epsilonEquals(wi, 1E-5f)) {
      return specular.reflectiveFilter(inputRadiance);
    }
    return NORADIANCE;
    
  }
  
}
