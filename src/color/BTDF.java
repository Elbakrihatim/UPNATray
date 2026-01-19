package color;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.fma;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import org.jogamp.vecmath.Vector3f;
//
import static engine.RadianceRGB.NORADIANCE;
import color.densityfunction.NormalDistributionFunction;
import color.reflectance.DielectricReflectance;
import color.reflectance.ReflectanceRGB;
import engine.RadianceRGB;
//
public abstract class BTDF implements BSDF {
  
  public static final float AIR_RI = 1.0002926f;

  protected final DielectricReflectance dielectric;
  protected final NormalDistributionFunction ndf;
  
  protected BTDF (final ReflectanceRGB dielectric,
                  final float eta_int, final float eta_ext,
                  final NormalDistributionFunction ndf) { 
    this.dielectric = new DielectricReflectance(dielectric, eta_int, eta_ext);
    this.ndf = ndf;
  }
  
  @Override
  public final RadianceRGB reflectiveFilter (final boolean toOutside,
                                             final RadianceRGB inputRadiance,        
                                             final Vector3f wi,                                            
                                             final Vector3f wo,                                      
                                             final Vector3f n) {
    
    if ((inputRadiance == NORADIANCE) || inputRadiance.isNegligible())
      return NORADIANCE;
    // Si la radiancia incidente es nula o llega por debajo del plano tangente
    // (por debajo de la línea del horizonte, desde la semiesfera negativa
    // de direcciones), la radiancia de salida es nula.    
    final float cosi = min(1.0f, n.dot(wi));
    if (Math.signum(cosi) <= 0)
      return NORADIANCE;

    // Cuando la luz se transmite de un medio más denso a un medio menos denso,
    // la identidad de Snell solo es válida para ángulos de incidencia por debajo
    // de un valor crítico. Por encima de ese ángulo crítico solo hay fenómeno
    // de reflexión.
    final float r = dielectric.rho(toOutside);
    if (signum(r - 1.0f) > 0) {
      
      final float coso = min(1.0f, n.dot(wo));
      final float critical = fma(r * r, fma(coso, coso, -1), 1);
      // Ángulo por encima de valor crítico (expresión negativa dentro de raíz cuadrada)
      if (signum(critical) < 0)
        return dielectric.filter(inputRadiance);
      
    }
    
    // Las pruebas anteriores aseguran:
    // * inputRadiancia es un valor de radiancia significativo.
    // * wi indica una dirección dentro del hemisferio iluminante.
    // * No ocurre el fenómeno de reflexión interna total.    
    return reflectiveFilter(toOutside, inputRadiance, wi, wo, n, cosi);
    
  }
  
  @Override
  public final RadianceRGB transmissiveFilter (final boolean toOutside,
                                               final RadianceRGB inputRadiance,         
                                               final Vector3f wi,                                            
                                               final Vector3f wo,                                      
                                               final Vector3f m) {
    
    // Si la radiancia incidente es nula o despreciable, la radiancia devuelta
    // es nula.
    if ((inputRadiance == NORADIANCE) || inputRadiance.isNegligible())
      return NORADIANCE;
    
    // Si la radiancia incidente  llega por debajo del plano tangente
    // (por debajo de la línea del horizonte, desde la semiesfera negativa
    // de direcciones), la radiancia devuelta es nula.    
    final float cosi = min(1.0f, -m.dot(wi));
    if (Math.signum(cosi) <= 0) 
       return NORADIANCE;
    
    // Cuando la luz se transmite de un medio más denso a un medio menos denso,
    // la identidad de Snell solo es válida para ángulos de incidencia por debajo
    // de un valor crítico. Por encima de ese ángulo crítico no hay fenómeno
    // de transmisión.
    final float r = dielectric.rho(toOutside);
    if (signum(r - 1.0f) > 0) {
      
      final float coso = min(1.0f, m.dot(wo));
      final float critical = fma(r * r, fma(coso, coso, -1), 1);
      // Ángulo por encima de valor crítico (expresión negativa dentro de raíz cuadrada)
      if (signum(critical) < 0)
        return NORADIANCE;
      
    }
    
    // Las pruebas anteriores aseguran:
    // * inputRadiancia es un valor de radiancia significativo.
    // * wi indica una dirección dentro del hemisferio iluminante.
    // * No ocurre el fenómeno de reflexión interna total.
    return transmissiveFilter(toOutside, inputRadiance, wi, wo, m, cosi);
    
  }
  
  abstract protected RadianceRGB reflectiveFilter (final boolean toOutside,
                                                   final RadianceRGB inputRadiance,
                                                   final Vector3f wi,
                                                   final Vector3f wo,
                                                   final Vector3f n,
                                                   final float cosi);
  
  abstract protected RadianceRGB transmissiveFilter (final boolean toOutside,
                                                    final RadianceRGB inputRadiance,
                                                    final Vector3f wi,
                                                    final Vector3f wo,
                                                    final Vector3f m,
                                                    final float cosi);  
  
  @Override
  public final boolean isTransmissive () {
    return true;
  }

  public Vector3f getSpecularRefractionDirection (final boolean toOutside,
                                                  final Vector3f wo,
                                                  final Vector3f m) {
    
    final Vector3f wi = new Vector3f(0, 0, 0);
    
    final float r = dielectric.rho(toOutside);
    // cos(theta_o)
    final float coso = m.dot(wo);
    final float tir = fma(r * r, fma(coso, coso, -1), 1);
    if (signum(tir) < 0)
      return wi;
    // cos(theta_i)
    final float cosi = (float) sqrt(tir);
    wi.scaleAdd(-r, wo, wi); // -r * wo
    wi.scaleAdd(fma(r, coso, -cosi), m, wi); // (r * coso - cosi) * m
    
//    System.out.println(wo);
//    System.out.println(r);
//    System.out.println(m);
    
    return wi;

  }   
  
}