package color.reflectance;
/**
 * Clase para representar coeficientes de reflectancia
 * a aplicar en cada canal de radiacion.
 *
 * @author MAZ
 */
import engine.RadianceRGB;
import static java.lang.Math.min;
import static java.lang.Math.signum;
//
public class ReflectanceRGB {

  protected final float xr;
  protected final float xg;
  protected final float xb;
  
//  public ReflectanceRGB () {
//    this(0.0f, 0.0f, 0.0f);
//  }
  
  public ReflectanceRGB (final float xr, final float xg, final float xb) {
    if (!((signum(xr) >= 0) && (signum(xr - 1) <= 0) &&
          (signum(xg) >= 0) && (signum(xg - 1) <= 0) &&
          (signum(xb) >= 0) && (signum(xb - 1) <= 0)))
      throw new IllegalArgumentException("Filter with invalid coefficients");
    this.xr = xr;
    this.xg = xg;
    this.xb = xb;
  }  
  
  public ReflectanceRGB (final ReflectanceRGB r) {
    this.xr = r.xr;
    this.xg = r.xg;
    this.xb = r.xb;
  }  
  
  /**
   * Devuelve la fraccion de radiancia incidente que es reflejada
   * de acuerdo a los coeficiented de reflectancia.
   *
   * @param radiance: radiancia a filtrar
   * @return 
   */  
  public RadianceRGB filter (final RadianceRGB radiance) {
    return new RadianceRGB(xr * radiance.r,
                           xg * radiance.g,
                           xb * radiance.b);
  }
  
  /**
   * Devuelve la fraccion de radiancia incidente efectiva que
   * es reflejada de acuerdo a los coeficientes de reflectancia.
   *
   * @param cosi: coseno del ángulo de incidencia
   * @param radiance: radiancia a filtrar
   * @return 
   */  
  public RadianceRGB filter (final float cosi, final RadianceRGB radiance) {
    return new RadianceRGB(xr * cosi * radiance.r,
                           xg * cosi * radiance.g,
                           xb * cosi * radiance.b);
  }  

  /**
   * Devuelve la fraccion del valor de radiancia efectiva
   * que no es absorbida (en consecuencia, es reflejada).
   *
   * @param w: factor de efectividad de la radiancia entrante (depende de la BRDF)
   * @param cosi: coseno del angulo de incidencia con respecto a la normal
   * @param radiance: radiancia a filtrar
   * @return 
   */  
  RadianceRGB filter (final float w, final float cosi, final RadianceRGB radiance) {
    return new RadianceRGB(min(1.0f, w * xr) * cosi * radiance.r,
                           min(1.0f, w * xg) * cosi * radiance.g,
                           min(1.0f, w * xb) * cosi * radiance.b);
  }
  
  public boolean isOperative () {
    return signum(xr + xg + xb) > 0;
  }
  
  float reflectance () {
    return (xr + xg + xb);
  }
  
  static public boolean combinationIsConservative (final ReflectanceRGB f0,
                                                   final ReflectanceRGB f1) {
    // Ambos filtros se pueden combinar si la suma de la radiancia reflejada
    // en cada canal es menor o igual que la radiancia entrante por ese canal.
    return (f0 == null) || (f1 == null) ||
           ((Math.signum(f0.xr + f1.xr - 1.0f) <= 0) &&
            (Math.signum(f0.xg + f1.xg - 1.0f) <= 0) &&
            (Math.signum(f0.xb + f1.xb - 1.0f) <= 0));
  }
  
  public float getR () { return xr; }
  public float getG () { return xg; }  
  public float getB () { return xb; }
  
  /**
   * @return @see Object#toString()
   */
  @Override
  public String toString() {
    return "radiancia: {" + this.xr + ", " + this.xg + ", " + this.xb + "}";
  }  
  
}