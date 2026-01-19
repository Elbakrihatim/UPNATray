package light;

import engine.RadianceRGB;

/**
 * Clase para establecer la potencia irradiada
 * por cada canal (700.0nm, 546.1nm, 535.8nm)
 * de acuerdo a la distribucion de porcentajes
 * indicada.
 * 
 * Por ejemplo: para conseguir una fuente de luz de color magenta
 * se pueden establecer los coeficientes (0.8f, 0.0f, 0.2f) que
 * indican que el 80% de la potencia luminosa se irradia a traves
 * del canal rojo y el 20% a traves del canal azul.
 * 
 * Esa indicación de potencia luminosa es necesario que sea convertida
 * en la distribución de potencia radiante en cada canal. El constructor
 * calcula los coeficientes que corresponden a la cantidad de vatios
 * necesarios para conseguir un incremento de 1 unidad de luminancia
 * en el canal correspondiente.
 * 
 * Está completamente implementada.
 *
 * @author MAZ
 */

public final class SpectrumRGB {
  
  private static final float RADIANT_POWER_RATIO_R = +62.0962f; // ratio at 700.0 nm
  private static final float RADIANT_POWER_RATIO_G =  +1.3791f; // ratio at 546.1 nm
  private static final float RADIANT_POWER_RATIO_B =  +1.0000f; // ratio at 435.8 nm
  private static final float POWER_RATIO =
          RADIANT_POWER_RATIO_R +
          RADIANT_POWER_RATIO_G +
          RADIANT_POWER_RATIO_B;
  private static final float RELATIVE_RADIANT_POWER_RATIO_R = RADIANT_POWER_RATIO_R / POWER_RATIO;
  private static final float RELATIVE_RADIANT_POWER_RATIO_G = RADIANT_POWER_RATIO_G / POWER_RATIO;
  private static final float RELATIVE_RADIANT_POWER_RATIO_B = RADIANT_POWER_RATIO_B / POWER_RATIO;  

  private final float xr;
  private final float xg;
  private final float xb;
  
  public SpectrumRGB (final float xr, final float xg, final float xb) {
    if ((Math.signum(xr) < 0) || (Math.signum(xg) < 0) || (Math.signum(xb) < 0) ||
        (Math.signum(Math.abs((xr + xg + xb) - 1.0f)) != 0))
      throw new IllegalArgumentException("Bad spectrum");
    this.xr = xr * RELATIVE_RADIANT_POWER_RATIO_R;
    this.xg = xg * RELATIVE_RADIANT_POWER_RATIO_G;
    this.xb = xb * RELATIVE_RADIANT_POWER_RATIO_B;   
  }  
  
  RadianceRGB distribute (final float power) {
    return new RadianceRGB(power * xr, power * xg, power * xb);
  }
  
}