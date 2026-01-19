package color;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Vector3f;
//
import static engine.RadianceRGB.NORADIANCE;
import engine.RadianceRGB;
//
public interface BSDF {
  
  // Método para calcular radiancia reflejada en materiales dieléctricos y conductivos.
  // El argumento toOutside indica si la reflexión ocurre en el exterior
  // del material (true), o si ocurre en el interior (false).
  default public RadianceRGB reflectiveFilter (final boolean toOutside,
                                               final RadianceRGB inputRadiance,
                                               final Vector3f wi,                                          
                                               final Vector3f wo,                                      
                                               final Vector3f n) {
    return NORADIANCE;
  }
  
  // Método para calcular radiancia transmitida en materiales dieléctricos
  // El argumento toOutside indica si la radiancia se refracta al medio exterior
  // (true), o hacia el medio interior (false).
  default public RadianceRGB transmissiveFilter (final boolean toOutside,
                                                 final RadianceRGB inputRadiance,         
                                                 final Vector3f wi,                                     
                                                 final Vector3f wo,                                      
                                                 final Vector3f m) {
    return NORADIANCE;
  }
  
  public boolean isTransmissive ();
  
  default public Vector3f getSpecularReflectionDirection (final Vector3f wo,
                                                          final Vector3f n) {
    final Vector3f in = new Vector3f();
    in.scaleAdd(-2 * n.dot(wo), n, wo);
    in.negate();
    return in;
  } 
  
}