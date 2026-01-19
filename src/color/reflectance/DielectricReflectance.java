package color.reflectance;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.max;
import static java.lang.Math.fma;
//
import engine.RadianceRGB;
//
public class DielectricReflectance extends FresnelianReflectance {
  
  private final float eta_int;
  private final float eta_ext;
  private final float rho;
  private final float inv_rho;
  
  public DielectricReflectance (final ReflectanceRGB rfc,
                                final float eta_int,
                                final float eta_ext) {
    super(rfc);
    this.eta_int = eta_int;
    this.eta_ext = eta_ext;
    this.rho = eta_ext / eta_int;
    this.inv_rho = eta_int / eta_ext;    
  }

  public RadianceRGB reflectiveFilter (final float cosi, final float coso,
                                       final RadianceRGB radiance) {
    
    // El fenómeno de reflexión interna total se ha considerado en la clase BTDF.
    final float T = getUnpolarizedTransmitanceFactor(cosi, coso);        
    return filter(1 - T, radiance); 
    
  }

  public RadianceRGB transmissiveFilter (final float cosi, final float coso,
                                         final RadianceRGB radiance) {
    
    // El fenómeno de reflexión interna total se ha considerado en la clase BTDF.
    final float T = getUnpolarizedTransmitanceFactor(cosi, coso);        
    return filter(T, radiance);   
    
  }

  // Unpolarized light transmittance
  private float getUnpolarizedTransmitanceFactor (final float _cosi, final float _cost) {
    
    // La simetría de las expresiones hace innecesario considerar el sentido
    // de transmisión (de dentro hacia afuera, o viceversa). En trasmisión hacia
    // el exterior simplemente cambia la nomenclatura: la expresión de Ts sirve
    // como Tp, y la expresión de Tp sirve como Ts. 

    final float cosi = max(0, _cosi);
    final float cost = max(0, _cost);
    
    final float a = eta_int * cosi;
    final float b = eta_ext * cost;
    final float ts = 1 / (a + b);
    final float Ts = ts * ts;
    
    final float c = eta_ext * cosi;
    final float tp = 1 / fma(eta_int, cost, c);
    final float Tp = tp * tp;
    
    return 2 * a * b * (Ts + Tp);
    
  }  

  @Override
  public boolean isDielectric () {
    return true;
  }

  @Override
  public boolean isConductive () {
    return false;
  }
  
  public float eta_int () { return eta_int; }
  public float eta_ext () { return eta_ext; }  
  public float rho (final boolean toOutside) { return toOutside ? inv_rho : rho; }
  
}