package color.bsdf;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Vector3f;
//
import color.BTDF;
import color.reflectance.ReflectanceRGB;
import engine.RadianceRGB;
import static engine.RadianceRGB.NORADIANCE;
//
public final class IdealDielectric extends BTDF {

  public IdealDielectric (final ReflectanceRGB dielectric,
                          final float eta_int, final float eta_ext) {
    super(dielectric, eta_int, eta_ext, null);
  }

  @Override
  protected RadianceRGB transmissiveFilter (final boolean toOutside,
                                            final RadianceRGB inputRadiance,         
                                            final Vector3f wi,                                         
                                            final Vector3f wo,                                      
                                            final Vector3f m,
                                            final float cosi) {
    
    //final Vector3f _in = super.getSpecularRefractionDirection(toOutside, m, v);
    //if (in.epsilonEquals(_in, 1E-5f)) {
      final float coso = m.dot(wo);
      final float r = dielectric.rho(toOutside);
      final float r2 = r * r;
      return dielectric.transmissiveFilter(cosi, coso, inputRadiance).scale(r2);
    //}
    //System.out.println(_in + " " + in + " " + v);
    //return NORADIANCE;
    
  }

  @Override
  protected RadianceRGB reflectiveFilter (final boolean toOutside,
                                          final RadianceRGB inputRadiance,         
                                          final Vector3f wi,
                                          final Vector3f wo,                                     
                                          final Vector3f n,
                                          final float cosi) {
    // Solo refleja en la dirección de incidencia que la Ley de Snell
    // socia a los vectores wo (dirección de salida) y n (normal).
    // Si la dirección de incidencia wi no está sufcieintemente cerca
    // de la dirección _wi, se devuelve radiancia nula.
    final Vector3f _wi = super.getSpecularReflectionDirection(n , wo);
    if (_wi.epsilonEquals(wi, 1E-5f)) {
      final float coso = n.dot(wo);
      return dielectric.reflectiveFilter(cosi, coso, inputRadiance);
    }
    return NORADIANCE;    

  }
  
}
