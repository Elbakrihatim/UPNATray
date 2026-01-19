package light;
/**
 * Fuente luminosa spot
 *
 * @author MAZ
 */
import static java.lang.Math.signum;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import engine.RadianceRGB;
import model3d.group3d.Group3D;
import raytracer.Hit;
import raytracer.Ray;
//
public class Spot extends Light {
  
  /**
   * Vector de orientación de la fuente
   */
  private final Vector3f axis;

  /**
   * Coseno del ángulo de apertura
   */
  private final float apertureIndex;
  
  /**
   * Intensidad radiante en cada dirección
   */
  private final RadianceRGB radiantIntensity;  

  /**
   * Constructor:
   *
   * @param position Emplazamiento de la fuente
   * @param lookAt Pundo de referencia, hacia donde "enfoca" la fuente
   * @param aperture Ángulo de apertura EN GRADOS [0-90]
   * @param spectrum Espectro de radiación en canales RGB
   * @param power Potencia (intensidad) de emisión
   * @throws java.lang.IllegalArgumentException Sí
   */
  public Spot (
          final Point3f position,
          final Point3f lookAt,
          final float aperture,
          final SpectrumRGB spectrum,
          final float power) throws IllegalArgumentException {
    super(position, spectrum, power);    
    if (aperture > 90.0f) {
      throw new IllegalArgumentException("El ángulo de apertura proporcionado (" + aperture + "º) es inválido; debe estar comprendido entre 0º y 90º.");
    }
    axis = new Vector3f();
    axis.sub(lookAt, S);
    axis.normalize();
    apertureIndex = (float) Math.cos(Math.toRadians(aperture));
    final float r = (float) (power / (2 * (1 - apertureIndex) * Math.PI));
    radiantIntensity = spectrum.distribute(r);
  }

  
    @Override
    public RadianceRGB getRadianceAt (final Hit hit, final Group3D scene) {
        final Point3f P = hit.getPoint();
        final Vector3f PS = new Vector3f();
        PS.sub(S, P);  
        final float cosAxisI = -axis.dot(PS) / PS.length();
        if (apertureIndex <= cosAxisI) { 
            final Ray shadowRay = new Ray(P, PS);
            shadowRay.shift(); 
            final float distance = P.distance(S); 
            if (!scene.intersectAny(shadowRay, 0, distance)) {
                final float distancia2 = distance * distance;
                final float d2 = 1.0f / distancia2;
                return new RadianceRGB(d2, radiantIntensity);
            }
        }
        return RadianceRGB.NORADIANCE;
    }
  
  @Override
  public Vector3f getIncidenceDirection (final Point3f P) {
    final Vector3f PS = new Vector3f();
    PS.sub(S, P);
    PS.normalize();
    return PS;
  }  
  
  
}