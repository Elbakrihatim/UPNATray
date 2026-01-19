package light;
/**
 * Representa una fuente de luz direccional de sección circular.
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import engine.RadianceRGB;
import model3d.group3d.Group3D;
import static primitives.ExtendedOperators.opposite;
import raytracer.Hit;
import raytracer.Ray;
//
public class Directional extends Light {
  
  /**
   * Dirección de emisión de la fuente
   */
  private final Vector3f direction;  

  /**
   * Radio (al cuadrado) de la sección circular
   */
  private final float squareRadius;
  
  /**
   * Radiosidad en cada punto de superficie luminosa
   */
  private final RadianceRGB puntualRadiosity;  

  /**
   * Constructor.
   *
   * @param position Ubicación del centro de la fuente
   * @param lookAt Punto de referencia hacia donde apunta la fuente
   * @param spectrum Espectro de radiación en canales RGB
   * @param power Potencia (intensidad) de emisión
   * @param radius Radio de la sección circular
   */
    public Directional (
          final Point3f position,
          final Point3f lookAt,
          final float radius,          
          final SpectrumRGB spectrum,          
          final float power) {
    super(position, spectrum, power);
     // La dirección se almacena como se necesita para:
     // * generar el rayo de sombra,
     // * responder a la pregunta de dirección de incidencia.
    this.direction = new Vector3f();
    this.direction.sub(lookAt, position);
    this.direction.normalize();         
    this.squareRadius = radius * radius;
    final float r = (float) (power / (Math.PI * squareRadius));
    this.puntualRadiosity = spectrum.distribute(r);
  }  

  @Override
  public RadianceRGB getRadianceAt (final Hit hit, final Group3D scene) {
    final Point3f P = hit.getPoint();
    final Vector3f SP = new Vector3f();
    SP.sub(P,S);
    final float a = SP.dot(direction);
    if(a >= 0){
        final float distancePDirection = SP.dot(SP) - a*a;
        if (distancePDirection <= squareRadius){
            final Vector3f direccionIncidencia = getIncidenceDirection(P);
            final Ray shadowRay = new Ray(P, direccionIncidencia);
            shadowRay.shift();
            if(!scene.intersectAny(shadowRay, 0, a)){
                return puntualRadiosity;
            } 
        }
    }
    return RadianceRGB.NORADIANCE;
  }
  
    @Override
    public Vector3f getIncidenceDirection(final Point3f P) {
        final Point3f posicion = getPosition(P);
        final Vector3f direccionIncidencia = new Vector3f();
        direccionIncidencia.sub(posicion, P);
        direccionIncidencia.normalize();
        return direccionIncidencia;
    }

    public Point3f getPosition(final Point3f P) {
        final Vector3f SP = new Vector3f();
        SP.sub(P,S);
        final float a = SP.dot(direction);
        final Vector3f scaledDirection = new Vector3f(direction);
        scaledDirection.scale(a);
        final Point3f posicion = new Point3f(P);
        posicion.sub(scaledDirection);
        return posicion;
    }
}