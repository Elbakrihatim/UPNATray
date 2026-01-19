package light;
/**
 * Clase genérica para fuente luminosa
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import engine.RadianceRGB;
import model3d.group3d.Group3D;
import raytracer.Hit;
//
public abstract class Light { 

  /**
   * Posición de la fuente en la escena.
   */
  protected final Point3f S;

  /**
   * Potencia de emisión.
   *
   */
  protected final float power;

  /**
   * Fraccion de la potencia de emisión en cada canal RGB.
   */
  protected final SpectrumRGB emissionSpectrum;

  protected Light (final Point3f position, final SpectrumRGB spectrum, final float power) {
    this.S = position;
    this.emissionSpectrum = spectrum;
    this.power = power;
  }

  /**
   * Nivel de radiancia que aporta esta fuente al punto de impacto indicado.
   *
   * @param hit Punto de impacto y normal encapsulados en objeto de clase Hit
   * @param scene Grupo de objetos para los que se considera la sombra
   * @return Cero si el punto de impacto no recibe irradiancia desde esta fuente
   */
  public abstract RadianceRGB getRadianceAt (final Hit hit, final Group3D scene);

  /**
   * Consultor de punto de emplazamiento S
   *
   * @return
   */
  public Point3f getPosition () {
    return S;
  }
  
  /**
   * Consultor de dirección de incidencia: si el punto P está dentro del campo
   * iluminado por la fuente, entonces se devuelve la dirección de incidencia
   * desde la que P recibe la energía luminosa emitida por la fuente.
   *
   * @param P
   * @return
   */
  public abstract Vector3f getIncidenceDirection (final Point3f P);

}