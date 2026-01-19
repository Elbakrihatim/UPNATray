package light;
/**
 * Representa una fuente de luz omnidireccional
 */
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import engine.RadianceRGB;
import model3d.group3d.Group3D;
import raytracer.Hit;
import raytracer.Ray;
//
public class Omnidirectional extends Light {
  
  /**
   * Intensidad irradiada en cada dirección
   */
  private final RadianceRGB radiantIntensity;

  /**
   * Crea una nueva luz puntual.
   *
   * @param position Ubicación de la fuente de luz
   * @param spectrum Espectro de radiación en canales RGB
   * @param power Potencia (intensidad) de emisión
   */
  public Omnidirectional (
          final Point3f position,
          final SpectrumRGB spectrum,
          final float power) {
    super(position, spectrum, power);
    final float r = (float) (power / (4 * Math.PI));
    radiantIntensity = spectrum.distribute(r);
  }
  
  @Override
  public RadianceRGB getRadianceAt (final Hit hit, final Group3D scene) {
      final Point3f P = hit.getPoint();
      final Vector3f I = new Vector3f();
      I.sub(S,P);
      final Ray shadowRay = new Ray(P,I);
      shadowRay.shift();
      final float distance = P.distance(S);
      if(!scene.intersectAny(shadowRay,0,distance))
      {
          final float squaredDistance = distance* distance;
          final float d2= 1.0f/squaredDistance;
        return new RadianceRGB(d2,radiantIntensity);
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