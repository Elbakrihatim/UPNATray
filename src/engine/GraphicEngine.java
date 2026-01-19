package engine;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.awt.Color;
import java.util.Collection;
import java.util.Map;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
import org.jogamp.vecmath.TexCoord2f;
//
import static engine.RadianceRGB.NORADIANCE;
import color.BSDF;
import color.BTDF;
import color.VModel;
import light.Light;
import model3d.Model3D;
import model3d.group3d.Group3D;
import raytracer.Hit;
import raytracer.Ray;
import scene.Scene;
//
public final class GraphicEngine {

  private final boolean illumination;
  private final boolean falseLighting;
  private final int maxRecursionDepth;
  
  // 
  private Map<Model3D, VModel> vmap;
  private Group3D sceneObjects;
  private Collection<Light> lights;
  
  private GraphicEngine (final boolean illumination,
                         final boolean falseLighting,
                         final int maxRecursionDepth) {
    if (illumination && (maxRecursionDepth < 0))
      throw new IllegalArgumentException("Nivel máximo de recursión no puede ser negativo");
    this.illumination = illumination;
    this.falseLighting = falseLighting;
    this.maxRecursionDepth = maxRecursionDepth;      
  }
  
  public GraphicEngine (final int maxRecursionDepth) {
    this(true, false, maxRecursionDepth);
  }  
  
  public GraphicEngine (final boolean falseLighting) {
    this(false, falseLighting, 0);
  }  

  public Color getColor (final Hit hit) {
    final VModel vmodel = hit.getVisualModel();
    final TexCoord2f uv = hit.getTextureCoordenates();
    if (uv != null) {
      return vmodel.getColor(uv);
    } else
      return vmodel.getColor();
  }

  public Color getColor (final Hit hit, final Vector3f v) {

    // Calcula el ángulo de vista respecto a la dirección del vector director del rayo.
    final Vector3f n = hit.getNormal();  
    
    final float brightnessFactor;
    if (falseLighting) {
        final float x = -n.dot(v);
        final float nv = x; //(2 - x) * x;
        brightnessFactor = max(min(nv, 1.0f), 0.0f);
    } else
      brightnessFactor = 1.0f;

    // Obtiene el color base del objeto
    final Color color = getColor(hit);
    // Muestra el color más brillante cuanto menor sea el ángulo de vista.
    final float[] colorComponents = new float[3];
    color.getColorComponents(colorComponents);
    return new Color(brightnessFactor * colorComponents[0],
                     brightnessFactor * colorComponents[1],
                     brightnessFactor * colorComponents[2]);
    
  }
  
  /**
   *
   * @param viewVector vector de vista
   * @param scene objetos que componen la escena
   * @param lights colección de luminarias aplicadas a la escena
   * @param hit informacion del punto de interseccion
   * @param ray rayo de trazado inicial
   * @return
   */
  public Color getColor (final Vector3f viewVector,
                         final Scene scene,
                         final Collection<Light> lights,
                         final Hit hit,
                         final Ray ray) {
    
    final Vector3f v = ray.getDirection();

    if (!illumination)
      // Obtiene el color del objeto; calcula un falso sombreado en función
      // de la dirección de incidencia y de la normal en el punto de intersección.
      return getColor(hit, v);
    else {
      // Computación del valor de color para el pixel a partir
      // de la radiancia que recibe el punto R de partida del rayo
      // desde la dirección v; se aplica el convenio de representar
      // la dirección de incidencia en sentido opuesto al que llevan
      // los fotones.
      this.vmap = scene.getVMap();
      this.sceneObjects = scene.getObjects();
      this.lights = lights;
      // La radiancia incidente sobre R desde la dirección v corresponde a radiancia
      // saliente desde el punto de intersección P en dirección -v. Esa radiancia 
      // saliente depende de la radiancia recibida en el punto P y de las propiedades
      // del material de la superficie en que se encuentra P.
      // - Se lanza el proceso recursivo para computar el espectro de radiancia
      //   saliente desde el hit en dirección -v.
      // - La opuesta a la dirección v de incidencia se emplea como dirección
      //   de radiancia saliente desde el hit.
      final RadianceRGB incidentRadiance = getOutgoingRadiance(v, hit, 0);
      // El vector de vista es ortogonal al plano de proyección; se aplica factor
      // de Lambert para obtener radiancia incidente medida sobre el plano de 
      // incidencia (se recibe medida ortogonalmente a la dirección de propagación).
      final RadianceRGB effectiveRadiance = incidentRadiance.scale(viewVector.dot(v));
      // - A partir del espectro de radiancia incidente computado se obtiene
      //   la representación CIE-XYZ que corresponde a la sensación color que
      //   ese espectro produce en la retina humana.
      // - Finalmente, esa representación de color se traduce a coordenadas
      //   en el espacio de dispositivo sRGB D65.      
      return effectiveRadiance.getColor();
    }

  }

  /**
   * 
   * Devuelve el valor de radiancia saliente en dirección wi desde el hit.
   *
   * @param hit informacion asociada (punto, normal, objeto, material) al punto de intersección
   * @param wi dirección de la radiancia saliente desde el hit
   * @param depth nivel de recursion
   * @return radiancia saliente en dirección wi desde el hit
   */
  private RadianceRGB getOutgoingRadiance (final Vector3f wi,    
                                           final Hit hit,
                                           final int depth) {
    
    if (depth <= maxRecursionDepth) {    
    
      // El vector wi indica la dirección de radiancia incidente (conforme
      // al convenio de presentar sentido contrario al que llevan los fotones
      // incidentes) desde el punto de vista del receptor.
      //
      // Considerando un esquema de reflexión/transmisión basado en el punto
      // contenido en el hit, la dirección de salida wo de ese esquema es
      // la dirección opuesta de wi.
      final Vector3f wo = new Vector3f(wi);
      wo.negate();
      
      // Se considera el punto de partida del rayo con el que se ha obtenido el hit:
      // - Si ese punto está fuera del objeto tridimensional, eso indica que
      //   la radiancia saliente a computar se dirige al exterior del objeto
      //   tridimensional que corresponde al hit.
      // - Si ese punto está dentro del objeto tridimensional, eso indica que
      //   la radiancia saliente a computar se dirige al interior del objeto
      //   tridimensional que corresponde al hit.
      //
      // Por convenio, los algoritmos de intersección deben devolver una normal
      // hacia el semiespacio en el que se encuentra el punto de partida del rayo
      // (ese semiespacio se define respecto al plano tangente en el punto de intersección).
      //
      // Ese convenio permite preparar un hack para determinar si la radiancia
      // a computar va hacia el exterior o hacia el interior del objeto:
      // - El punto del hit se desplaza ligeramente en dirección de la normal.
      final Point3f X = hit.getShiftedPoint(); 
      // - Si la normal en el punto de intersección apunta al exterior del objeto
      //   tridimendional, el punto X estará fuera del objeto tridimensional que
      //   corresponde al hit.
      // - Si la normal en el punto de intersección apunta al interior del objeto
      //   tridimendional, el punto X estará dentro del objeto tridimensional que
      //   corresponde al hit.
      final Model3D object = hit.getModel3D(); 
      return toOutsideFrom(hit, wo, depth);
      // - El resultado del test determina el cómputo a realizar.
      //   * X fuera del objeto implica radiancia saliente hacia el exterior del objeto: se invoca toOutsideFrom()
      //   * X dentro del objeto implica radiancia saliente hacia el interior del objeto: se invoca toInsideFrom()
      //return object.isOutside(X) ?       
      //  toOutsideFrom(hit, wo, depth) :        
      //  toInsideFrom(hit, wo, depth);
    
    } else
      return NORADIANCE;
    
  }  
  
  /**
   * 
   * Devuelve el valor de radiancia saliente en dirección wo hacia el exterior
   * del objeto al que se refiere el hit.
   * - Calcula la radiancia incidente sobre el punto P del hit:
   *   * Radiancia que incide directamente desde las fuentes de luz.
   *   * Radiancia que incide indirectamente reflejada/trasmitida desde
   *   * las superficies de objetos de la escena.
   * - La radiacia saliente se obtiene aplicando a la radiancia incidente
   *   el correspondiente filtro (reflectivo/transmisivo) de la función
   *   de reflectancia BSDF asociada al material del punto de intersección.
   *
   * @param hit informacion  (punto, normal, material) del punto de interseccion
   * @param wo dirección de la radiancia saliente
   * @param depth nivel de recursion
   * @return
   */
  private RadianceRGB toOutsideFrom (final Hit hit,                                              
                                     final Vector3f wo,
                                     final int depth) {

    // Radiancia saliente hacia el exterior del objeto del hit.
    final boolean toOutside = true;

    // Se calcula y acumula radiancia recibida directamente desde fuentes de luz.
    // Se calcula y acumula radiancia reflejada indirectamente desde otras superficies.
    // Si el medio es dieléctrico, se calcula y acumula radiancia incidente desde interior.
    final RadianceRGB outgoingRadiance = new RadianceRGB();

    final Model3D object = hit.getModel3D();

    // Función de reflectancia/transmitancia
    final BSDF bsdf = vmap.get(object).getBSDF();
    
    // Radiancia de fuentes sobre hit que es reflejada/transmitida en dirección wo.
    outgoingRadiance.add(getRadianceFromLights(hit, wo, bsdf));

    // Punto y normal; junto con wo son los elementos fijos
    // a partir de los que componer el esquema de la ley de Snell.
    final Point3f P = hit.getPoint();
    final Vector3f n = hit.getNormal();

    // Cómputo de radiancia incidente sobre P reflejada desde otras superficies
    // Todo material tiene capacidad reflexiva, así que este es un caso fijo.
    // La reflexión ocurre en el medio exterior al objeto.
    {

      // Única dirección de sampleo (solo se consideran materiales ideales)
      final Vector3f wi = bsdf.getSpecularReflectionDirection(wo, n);

      // Rayo secundario para encontrar objeto que refleje hacia P.
      // Se lanza desde P + epsilon * wi para evitar autointersección.      
      final Ray ray = new Ray(P, wi);
      ray.shift();

      // Se estudia la intersección con el rayo secundario.
      final Hit _hit = sceneObjects.intersect(ray);
      if (_hit.hits()) {

        // Total de radiancia incidente sobre P reflejada desde la intersección obtenida.
        // - Vector director wi: indica la dirección de incidencia sobre P; su
        //   dirección es opuesta a la de la radiancia reflejada desde _hit.
        // - Hit _hit: información del punto de intersección obtenido por el rayo
        //   secundario.
        RadianceRGB indirectRadiance = getOutgoingRadiance(wi, _hit, depth + 1);
        // Fracción de esa radiancia incidente desde la dirección wi
        // que se refleja de modo especular en dirección wo.
        final RadianceRGB reflectedRadiance
          = bsdf.reflectiveFilter(toOutside, indirectRadiance, wi, wo, n);
        outgoingRadiance.add(reflectedRadiance);

      }

    }
        
    return outgoingRadiance;

  }
  
  private RadianceRGB toInsideFrom (final Hit hit,                                              
                                    final Vector3f wo,
                                    final int depth) {
    // Radiancia saliente hacia interior del objeto del hit.
    final boolean toOutside = false;

    // Se calcula y acumula radiancia recibida directamente desde fuentes luminosas.
    // Se calcula y acumula radiancia reflejada indirectamente desde otras superficies.
    // Si el medio es dieléctrico, se calcula y acumula radiancia incidente desde interior.
    final RadianceRGB outgoingRadiance = new RadianceRGB();   
    
    return outgoingRadiance;

  }
  
  private RadianceRGB getRadianceFromLights (final Hit hit,
                                             final Vector3f wo,
                                             final BSDF bsdf) {
    
    final RadianceRGB outgoingRadiance = new RadianceRGB();    
    
    final boolean toOutside = true;
    
    final Point3f P = hit.getPoint();
    final Vector3f n = hit.getNormal();
    for (final Light light: lights) {

      final Vector3f wi = light.getIncidenceDirection(P);
      final RadianceRGB inputRadiance = light.getRadianceAt(hit, sceneObjects);
      if (inputRadiance != NORADIANCE) {
        final RadianceRGB reflectedRadiance
          = bsdf.reflectiveFilter(toOutside, inputRadiance, wi, wo, n);
        outgoingRadiance.add(reflectedRadiance);
      }
      
    }
      
    return outgoingRadiance;
      
  }

}