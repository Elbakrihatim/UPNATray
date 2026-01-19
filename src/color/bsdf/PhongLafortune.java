package color.bsdf;
/*
 *
 * @author MAZ
 */
import static java.lang.Math.abs;
import org.jogamp.vecmath.Vector3f;
//
import color.BRDF;
import color.densityfunction.NormalDistributionFunction;
import color.reflectance.ReflectanceRGB;
import engine.RadianceRGB;
//
public final class PhongLafortune extends BRDF {
 
  public PhongLafortune (final ReflectanceRGB diffuse,
                         final ReflectanceRGB specular,
                         final NormalDistributionFunction ndf) {
    super(diffuse, specular, ndf);
  }
  
  @Override
  public RadianceRGB diffuseFilter (final RadianceRGB inputRadiance,
                                    final Vector3f wi,
                                    final Vector3f wo,
                                    final Vector3f n,
                                    final float cosi) {  
    // Radiancia saliente debida a reflexión difusa; se aplica el filtro
    // con los coeficientes de reflexión difusa a la radiancia de entrada efectiva.
    // Radiancia de entrada efectiva = radiancia incidente por coseno del ángulo de incidencia
    return diffuse.filter(cosi, inputRadiance);
    
  }
  
  @Override
  public RadianceRGB reflectiveFilter (final RadianceRGB inputRadiance,
                                       final Vector3f wi,
                                       final Vector3f wo,
                                       final Vector3f n,
                                       final float cosi) {
    // Vector mediocamino
    final Vector3f h = new Vector3f();
    h.add(wi, wo);
    h.normalize();
    
    // Proporción de microfacetas con normal orientada hacia el vector mediocamino 
    final float x = ndf.getProbability(n, h);
    
    // Factor para devolver radiancia medida ortogonalmente a la direción de salida.
    final float coso = n.dot(wo);    
    
    final float w = 1 / coso;
    
    //System.out.println(x + " " + w +  " " + specular.filter(cosi, inputRadiance).scale(w));
    
    // Radiancia saliente debida a reflexión glossy; se aplica el filtro
    // con los coeficientes de reflexión especular a la radiancia de entrada efectiva.
    // Obsérvese que la radiancia incidente efectiva se multiplica además por la fracción
    // de microfacetas orientadas en la dirección del vector medio-camino.
    // En otras BRDFs, es necesario elaborar más ese factor.    
    return specular.filter(cosi, inputRadiance).scale(abs(w));
  }
    
}