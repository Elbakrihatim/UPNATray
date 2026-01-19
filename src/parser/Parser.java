package parser;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.signum;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//
import static color.BTDF.AIR_RI;
import color.BRDF;
import color.BTDF;
import color.VModel;
import color.bsdf.AshikhminShirley;
import color.bsdf.IdealConductor;
import color.bsdf.IdealDielectric;
import color.bsdf.OrenNayar;
import color.bsdf.PhongLafortune;
import color.bsdf.ReflectiveCookTorrance;
import color.bsdf.TransmissiveCookTorrance;
import color.densityfunction.Beckmann;
import color.densityfunction.Blinn;
import color.densityfunction.GGX;
import color.densityfunction.Horn;
import color.densityfunction.NormalDistributionFunction;
import color.densityfunction.Torrance;
import color.reflectance.ReflectanceRGB;
import engine.GraphicEngine;
import static java.lang.Math.toRadians;
import light.Directional;
import light.Light;
import light.Omnidirectional;
import light.SpectrumRGB;
import light.Spot;
import model3d.Model3D;
import model3d.Transform;
import static model3d.Transform.IDENTITY_TRANSFORM;
import static model3d.Transform.getRotationMatrix;
import static model3d.Transform.getScaleMatrix;
import static model3d.Transform.getTranslationMatrix;
import model3d.shape.Box;
import model3d.shape.Capsule;
import model3d.shape.Cylinder;
import model3d.shape.Ellipsoid;
import model3d.shape.Genus2;
import model3d.shape.Plane;
import model3d.shape.Shape3D;
import model3d.shape.Sphere;
import model3d.shape.Torus;
import model3d.shape.TruncatedCone;
import org.jogamp.vecmath.Matrix4f;
import org.jogamp.vecmath.Tuple3f;
import raytracer.RayTracer;
import scene.Scene;
import view.Camera;
import view.Angular;
import view.Orthographic;
import view.Perspective;
import view.Projection;
import view.NLPerspective;
import static model3d.Transform.getAimingMatrix;
import model3d.shape.Egg;
//
public final class Parser {

  private final Map<String, Shape3D> gmodelCollection;  
  private final Map<String, VModel> vmodelCollection;
  private final int W;
  private final int H;
  private final Color background;
  private final String imageName;
  private final GraphicEngine engine;
  private final RayTracer rayTracer;
  private final Camera camera;
  private final Projection projection;
  private final Scene scene;
  private final Collection<Light> lights;

  public Parser (final File in) throws ParserConfigurationException, SAXException, IOException  {

    final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    final Document doc = dBuilder.parse(in);
    
    final NodeList rootElementList = doc.getElementsByTagName("image");
    
    if (rootElementList.getLength() > 0) {
      
      final Element rootElement = (Element) rootElementList.item(0);
      this.imageName = rootElement.getAttribute("name");
      this.W = this.parseW(rootElement);
      this.H = this.parseH(rootElement);
      this.background = this.parseBackgroundColor(rootElement);
      this.projection = this.parseProjection(rootElement);      
      this.camera = this.parseCamera(rootElement);
      this.rayTracer = this.parseRayTracer(rootElement);
      this.engine = this.parseEngine(rootElement);
      this.lights = this.parseLights(rootElement);
      this.vmodelCollection = this.parseMaterials(rootElement);
      this.gmodelCollection = this.parseShapeModels(rootElement);
      System.out.println("Construidos los modelos geométricos");
      this.scene = this.parseScene(rootElement);
      System.out.println("Escena completada");
    
    }
    else {
      throw new SAXException("Elemento <image> no encontrado");
    }

  }
  
  private int parseW (final Element doc) throws SAXException {
    
    final NodeList rasterNodeList = doc.getElementsByTagName("raster");
  
    if (rasterNodeList.getLength() > 0) {

      final Element el = (Element) rasterNodeList.item(0);
      final int width = Integer.parseInt(
              el.getElementsByTagName("W").item(0).getTextContent());

      return width;
      
    } else {
      throw new SAXException("Elemento <raster> no encontrado");
    }
    
  }
  
  private int parseH (final Element doc) throws SAXException {
    
    final NodeList rasterNodeList = doc.getElementsByTagName("raster");
  
    if (rasterNodeList.getLength() > 0) {

      final Element el = (Element) rasterNodeList.item(0);
      final int height = Integer.parseInt(
              el.getElementsByTagName("H").item(0).getTextContent());

      return height;
      
    } else {
      throw new SAXException("Elemento <raster> no encontrado");
    }
    
  }
  
  private RayTracer parseRayTracer (final Element doc) throws SAXException {
    
    final NodeList tracerNodeList = doc.getElementsByTagName("rayTracer");    
  
    if (tracerNodeList.getLength() > 0) {
      
      final Element el = (Element) tracerNodeList.item(0);
      
      final boolean progress = Boolean.parseBoolean(
              el.getElementsByTagName("showProgress").item(0).getTextContent().toLowerCase());

      final int wblock = Integer.parseInt(el.getElementsByTagName("wblock").item(0).getTextContent());
      final int hblock = Integer.parseInt(el.getElementsByTagName("hblock").item(0).getTextContent());
      
      return new RayTracer(wblock, hblock, getCamera(), progress);
      
    } else {
      throw new SAXException("Elemento <rayTracer> no encontrado");
    }
    
  }  
  
  private GraphicEngine parseEngine (final Element doc) throws SAXException {
    
    final NodeList engineNodeList = doc.getElementsByTagName("engine");
  
    if (engineNodeList.getLength() > 0) {
      
      final Element el = (Element) engineNodeList.item(0);

      final boolean illumination = Boolean.parseBoolean(
              el.getElementsByTagName("illumination").item(0).getTextContent().toLowerCase());

      if (illumination) {
        final NodeList recursionDepthNodeList = el.getElementsByTagName("recursionDepth");
        final int recursionDepth = (recursionDepthNodeList.getLength() > 0) ?
          Integer.parseInt(recursionDepthNodeList.item(0).getTextContent()) : 0;
        return new GraphicEngine(recursionDepth);
      } else {
        final NodeList falseLightingNodeList = el.getElementsByTagName("falseLighting");
        final boolean falseLighting = (falseLightingNodeList.getLength() > 0) ?
          Boolean.parseBoolean(falseLightingNodeList.item(0).getTextContent()) : true;          
        return new GraphicEngine(falseLighting);
      }
      
    } else {
      throw new SAXException("Elemento <engine> no encontrado");
    }
    
  }
  
  private Color parseBackgroundColor (final Element doc) throws SAXException {
    
    final NodeList rasterNodeList = doc.getElementsByTagName("raster");
  
    if (rasterNodeList.getLength() > 0) {

      final Element el = (Element) rasterNodeList.item(0);
      final Color backgroundColor = this.parseColor(
              el.getElementsByTagName("backgroundColor").item(0).getTextContent());

      return backgroundColor;
      
    } else {
      throw new SAXException("Elemento <raster> no encontrado");
    }
    
  }  

  private Camera parseCamera (final Element doc) throws SAXException {
    
    final NodeList cameraNodeList = doc.getElementsByTagName("camera");
    
    if (cameraNodeList.getLength() > 0) {
      final Element el = (Element) cameraNodeList.item(0);
      final Point3f pos = this.parsePoint3f(
              el.getElementsByTagName("position").item(0).getTextContent());
      final Point3f lookAt = this.parsePoint3f(
              el.getElementsByTagName("lookAt").item(0).getTextContent());
      final Vector3f up = this.parseVector3f(
              el.getElementsByTagName("up").item(0).getTextContent());

      return new Camera(pos, lookAt, up);
      
    } else {
      throw new SAXException("Elemento <camera> no encontrado");
    }
    
  }

  private Projection parseProjection (final Element doc) throws SAXException {
    
    final NodeList projectionNodeList = doc.getElementsByTagName("projection");
    
    if (projectionNodeList.getLength() > 0) {
      final Element el = (Element) projectionNodeList.item(0);

      final Projection _projection;
      switch (el.getAttribute("type")) {
        
        case "perspective": {
          final float fov = Float.parseFloat(
                  el.getElementsByTagName("fov").item(0).getTextContent());
          final float aspect;
          if (el.getElementsByTagName("aspect").getLength() != 0) {
            aspect = Float.parseFloat(
                    el.getElementsByTagName("aspect").item(0).getTextContent());
          } else
            aspect = 1;

          _projection = new Perspective(fov, aspect);
        }
        break;

        case "orthographic": {
          final float height = Float.parseFloat(
                    el.getElementsByTagName("height").item(0).getTextContent());
          final float aspect;
          if (el.getElementsByTagName("aspect").getLength() != 0) {
            aspect = Float.parseFloat(
                    el.getElementsByTagName("aspect").item(0).getTextContent());
          } else
            aspect = 1;

          _projection = new Orthographic(height, aspect);
        }
        break;

        case "nlperspective": {
          final float fov = Float.parseFloat(
                  el.getElementsByTagName("fov").item(0).getTextContent());
          final float f;
          if (el.getElementsByTagName("depthFactor").getLength() > 0) {
            f = Float.parseFloat(
                    el.getElementsByTagName("depthFactor").item(0).getTextContent());
          } else {
            f = 1.0f;
          }

          _projection = new NLPerspective(fov);
        }
        break;  
          
        case "angular": {         
          final float fov = Float.parseFloat(
                  el.getElementsByTagName("fov").item(0).getTextContent());          

          _projection = new Angular(fov);
        }
        break;

        default: {
          _projection = null;
        }
      }

      return _projection;

    } else {
      throw new SAXException("Elemento <projection> no encontrado");
    }
    
  }
  
  private Collection<Light> parseLights (final Element doc) {
    
    final Collection<Light> g = new ArrayList<>();

    final NodeList lightsElementList = doc.getElementsByTagName("lights");
    if (lightsElementList.getLength() > 0) {

      for (int k = 0; k < lightsElementList.getLength(); ++k) {

        final Element lightsElement = (Element) lightsElementList.item(k);
        final NodeList lightsList = lightsElement.getElementsByTagName("light");
        for (int j = 0; j < lightsList.getLength(); ++j) {
          final Element lightElement = (Element) lightsList.item(j);
          g.add(this.parseLight(lightElement));
        }

      }

    }

    return g;
    
  }

  private Light parseLight (final Element el) {
     
    final SpectrumRGB spectrum = this.parseSpectrum(
            el.getElementsByTagName("spectrum").item(0).getTextContent());
    final float power = Float.parseFloat(
            el.getElementsByTagName("power").item(0).getTextContent());   
    
    final Light light;
    switch (el.getAttribute("type")) {

      case "omnidirectional": {
        final Point3f position = this.parsePoint3f(
                el.getElementsByTagName("position").item(0).getTextContent());          
        light = new Omnidirectional(position, spectrum, power);
      }
      break;

      case "directional": {
        final Point3f position = this.parsePoint3f(
                el.getElementsByTagName("position").item(0).getTextContent());          
        final Point3f lookAt = this.parsePoint3f(
                el.getElementsByTagName("lookAt").item(0).getTextContent());
        final float radius = Float.parseFloat(
                el.getElementsByTagName("radius").item(0).getTextContent());
//        final String attenuationExponent =
//                (el.getElementsByTagName("attenuationExponent").getLength() > 0)
//                ? el.getElementsByTagName("attenuationExponent").item(0).getTextContent()
//                : null;
//
//        if (attenuationExponent.length() > 0) {
//          final float attExponent = Float.parseFloat(attenuationExponent);
//          light = new Directional(position, lookAt, radius, spectrum, power, attExponent);
//        } else {
//          light = new Directional(position, lookAt, radius, spectrum, power);
//        }

        light = new Directional(position, lookAt, radius, spectrum, power);
      
      }
      break;

      case "spot": {
        final Point3f position = this.parsePoint3f(
                el.getElementsByTagName("position").item(0).getTextContent());          
        final Point3f lookAt = this.parsePoint3f(
                el.getElementsByTagName("lookAt").item(0).getTextContent());
        final float aperture = Float.parseFloat(
                el.getElementsByTagName("aperture").item(0).getTextContent());

        light = new Spot(position, lookAt, aperture, spectrum, power);
      }
      break;
        
      default: {
        light = null;
      }
        
    }

    return light;
  }

  private SpectrumRGB parseSpectrum (final String c) {
    
    final StringTokenizer st = new StringTokenizer(c);
    final float r = Float.parseFloat(st.nextToken());
    final float g = Float.parseFloat(st.nextToken());
    final float b = Float.parseFloat(st.nextToken());

    return new SpectrumRGB(r, g, b);
  }  
  
  private Map<String, VModel> parseMaterials (final Element doc) {
    
    final Map<String, VModel> map = new HashMap<>();
    
    final NodeList bsdfElementList = doc.getElementsByTagName("materials");
    for (int k = 0; k < bsdfElementList.getLength(); ++k) {

      final Element btdfElement = (Element) bsdfElementList.item(k);
      
      // Materiales no transmisores
      final NodeList brdfs = btdfElement.getElementsByTagName("brdf");
      for (int j = 0; j < brdfs.getLength(); ++j) {
        final Element el = (Element) brdfs.item(j);
        this.parseBRDF(el, map);
      }
      
      // Materiales transmisores
      final NodeList btdfs = btdfElement.getElementsByTagName("btdf");
      for (int j = 0; j < btdfs.getLength(); ++j) {
        final Element el = (Element) btdfs.item(j);
        this.parseBTDF(el, map);
      }      

    }
    
    return map;
    
  }  
  
  private VModel parseBRDF (final Element el, final Map<String, VModel> map) {

    final String id = el.getAttribute("id");
    
    final float sigma;
    final NodeList _sigma = el.getElementsByTagName("sigma");
    if (_sigma.getLength() > 0) {
      sigma = Float.parseFloat(_sigma.item(0).getTextContent());
    } else
      sigma = 0.0f;
    
    final float alpha;
    final NodeList _alpha = el.getElementsByTagName("alpha");
    if (_alpha.getLength() > 0) {
      alpha = Float.parseFloat(_alpha.item(0).getTextContent());
    } else
      alpha = 0.0f;      

    final ReflectanceRGB diffuse;
    final NodeList _diffuse = el.getElementsByTagName("diffuseReflectance");
    if (_diffuse.getLength() > 0) {
      diffuse = parseFilter(_diffuse.item(0).getTextContent());       
    } else
      diffuse = null;
    
    final ReflectanceRGB conductive;
    final NodeList _conductive = el.getElementsByTagName("conductiveReflectance");
    if (_conductive.getLength() > 0) {
      conductive = parseFilter(_conductive.item(0).getTextContent());
    } else
      conductive = null;

    final NormalDistributionFunction ndf = parseNDF(el);
    
    final BRDF shader;
    switch (el.getAttribute("type")) {
      
      case "Phong":
      case "Phong-Lafortune": {        
        shader = new PhongLafortune(diffuse, conductive, ndf);
      }
      break;
        
      case "Ashikhmin-Shirley": {
        shader = new AshikhminShirley(diffuse, conductive, ndf);
      }
      break;
      
      case "Cook-Torrance": {
        shader = new ReflectiveCookTorrance(diffuse, conductive, ndf);
      }
      break;     
      
      case "Oren-Nayar": {
        shader = new OrenNayar(diffuse, sigma);
      }
      break;
      
//      case "Ward": {
//        shader = new Ward(diffuse, conductive, ndf);
//      }
//      break;      
      
//      case "K3": {
//        shader = new K3(diffuse, conductive, ndf, alpha);
//      }
//      break;
      
      case "Ideal-Conductor": {
        shader = new IdealConductor(conductive);
      }
      break;      
      
      default: {
        shader = new PhongLafortune(diffuse, conductive, ndf);
      }

    }
    
    final VModel material = new VModel(shader);
    map.put(id, material);
    
    return material;
    
  }
  
  private VModel parseBTDF (final Element el, final Map<String, VModel> map) {

    final String id = el.getAttribute("id");   
    
    // Refraction index of dielectric and conductor materials
    final float eta_int;
    final NodeList _eta_int = el.getElementsByTagName("eta_int");
    if (_eta_int.getLength() > 0) {
      eta_int = Float.parseFloat(_eta_int.item(0).getTextContent());
    } else
      eta_int = Float.POSITIVE_INFINITY;
    
    final float eta_ext;
    final NodeList _eta_ext = el.getElementsByTagName("eta_ext");
    if (_eta_ext.getLength() > 0) {
      eta_ext = Float.parseFloat(_eta_ext.item(0).getTextContent());
    } else
      eta_ext = AIR_RI; 
    
    final ReflectanceRGB dielectric;
    final NodeList _dielectric = el.getElementsByTagName("dielectricReflectance");
    if (_dielectric.getLength() > 0) {
      dielectric = parseFilter(_dielectric.item(0).getTextContent());
    } else
      dielectric = new ReflectanceRGB(0.0f, 0.0f, 0.0f);
    
    final NormalDistributionFunction ndf = parseNDF(el);    
    
    final BTDF shader;
    switch (el.getAttribute("type")) {
      
      case "Cook-Torrance": {
        shader = new TransmissiveCookTorrance(dielectric, eta_int, eta_ext, ndf);
      }
      break; 
      
      case "Ideal-Dielectric":
      default: {
        shader = new IdealDielectric(dielectric, eta_int, eta_ext);
      }
      
    }

    final VModel material = new VModel(shader);
    map.put(id, material);
    
    return material;

  }
  
  private ReflectanceRGB parseFilter (final String c) {
    
    final StringTokenizer st = new StringTokenizer(c);
    final float r = Float.parseFloat(st.nextToken());
    final float g = Float.parseFloat(st.nextToken());
    final float b = Float.parseFloat(st.nextToken());

    return new ReflectanceRGB(r, g, b);
  }
  
  private NormalDistributionFunction parseNDF (final Element el) {
    
    final float beta;
    final NodeList _beta = el.getElementsByTagName("beta");
    if (_beta.getLength() > 0) {
      beta = Float.parseFloat(_beta.item(0).getTextContent());
    } else
      beta = 0.0f;    
    
    final NodeList _ndf = el.getElementsByTagName("ndf");    
        
    final NormalDistributionFunction ndf;
    if (_ndf.getLength() > 0) {

      final String densityFunctionName = _ndf.item(0).getTextContent();
      switch (densityFunctionName) {
        
        case "Horn": {
          ndf = new Horn(beta);
        }
        break;        
        
        case "Blinn": {
          ndf = new Blinn(beta);
        }
        break;
          
        case "Torrance": {
          ndf = new Torrance(beta);
        }
        break;         
        
        case "GGX": {
          ndf = new GGX(beta);
        }
        break;

        case "Beckmann": {
          ndf = new Beckmann(beta);
        }
        break;         
          
        default: {
          //System.out.println("Default PDF");
          ndf = new Blinn(beta);
        }           
                
      }
      
    }
    else
      ndf = new Blinn(beta);
        
    return ndf;
  } 
  
  private Map<String, Shape3D> parseShapeModels (final Element doc) throws SAXException, IOException {
    
    final Map<String, Shape3D> map = new HashMap<>();
    
    final NodeList modelsElementList = doc.getElementsByTagName("models");
    for (int k = 0; k < modelsElementList.getLength(); ++k) {

      final Element shadersElement = (Element) modelsElementList.item(0);
      final NodeList shaders = shadersElement.getElementsByTagName("model");
      for (int j = 0; j < shaders.getLength(); ++j) {
        final Element el = (Element) shaders.item(j);
        this.parseShape(el, map);
      }

    }
    
    return map;
    
  }

  private Shape3D parseShape (final Element element,
                              final Map<String, Shape3D> map) throws SAXException, IOException {

    final String id = element.getAttribute("id");
        
    final Shape3D model;     
    switch (element.getAttribute("type")) {
      
      case "sphere": {
        model = parseSphere(element);
      }
      break;
      
      case "box": {
        model = parseBox(element);
      }
      break;
      
      case "plane": {
        model = parsePlane(element);
      }
      break;
      
      case "cylinder": {
        model = parseCylinder(element);
      }
      break;      
      
      case "ellipsoid": {
        model = parseEllipsoid(element);
      }
      break;
      
      case "genus2": {
        model = parseGenus2(element);
      }
      break;        

      case "capsule": {
        model = parseCapsule(element);
      }
      break;    
      
      case "torus": {
        model = parseTorus(element);
      }
      break;
      
//      case "cyclide": {
//        model = parseCyclide(element);
//      }
//      break;      
      
      case "truncone": {
        model = parseTruncatedCone(element);
      }
      break;      

      case "mesh": {
        model = parsePolygonalMesh(element); 
      } 
      break; 
      case "egg":{
          model = parseEgg(element);
      }
      break;
      
      default: {
        throw new SAXException("Modelo geométrico desconocido: " + element.getAttribute("type"));
      }
      
    }
    
    if (id.length() > 0)
      map.put(id, model);

    return model;
    
  }  

  private Scene parseScene (final Element doc) throws SAXException, IOException {
      
    final NodeList sceneNodeList = doc.getElementsByTagName("scene");
    
    if (sceneNodeList.getLength() > 0) {

      final Scene _scene = new Scene();

      final Element sceneElement = (Element) sceneNodeList.item(0);
      final NodeList objects = sceneElement.getElementsByTagName("object");
      for (int j = 0; j < objects.getLength(); ++j) {
        final Element el = (Element) objects.item(j);
        final Model3D model3d = parseModel3D(el);
        final VModel vmodel = parseVisualModel(el);
        _scene.addObject(model3d, vmodel);
      }

      return _scene;
      
    } else {
      throw new SAXException("Elemento <scene> no encontrado");
    }   
    
  }  

  private Model3D parseModel3D (final Element element) throws SAXException, IOException {   

    final String modelId
            = element.getElementsByTagName("modelId").item(0).getTextContent();
    final Shape3D model = gmodelCollection.get(modelId);
    final Transform transform = parseAffineTransformation(element);
    
    return new Model3D(model, transform);
    
  }

  private Color parseColor (final String c) {
    
    final StringTokenizer st = new StringTokenizer(c);
    float r = Float.parseFloat(st.nextToken());
    float g = Float.parseFloat(st.nextToken());
    float b = Float.parseFloat(st.nextToken());

    r = r > 1.0f ? r / 255f : r;
    g = g > 1.0f ? g / 255f : g;
    b = b > 1.0f ? b / 255f : b;
    
    return new Color(r, g, b); 
  }

  private Vector3f parseVector3f (final String v) {
    
    final StringTokenizer st = new StringTokenizer(v);
    final float x = Float.parseFloat(st.nextToken());
    final float y = Float.parseFloat(st.nextToken());
    final float z = Float.parseFloat(st.nextToken());

    return new Vector3f(x, y, z);
  }

  private Point3f parsePoint3f (final String p) {
    
    final StringTokenizer st = new StringTokenizer(p);
    final float x = Float.parseFloat(st.nextToken());
    final float y = Float.parseFloat(st.nextToken());
    final float z = Float.parseFloat(st.nextToken());

    return new Point3f(x, y, z);
  }
  
  private Point2f parsePoint2f (final String p) {
    
    final StringTokenizer st = new StringTokenizer(p);
    final float u = Float.parseFloat(st.nextToken());
    final float v = Float.parseFloat(st.nextToken());

    return new Point2f(u, v);
  }  
  
  public GraphicEngine getEngine () {
    return engine;
  }
  
  public RayTracer getRayTracer() {
    return rayTracer;
  }
  
  public Camera getCamera () {
    camera.setProjection(projection);
    return camera;
  }
  
  public Scene getScene () {
    return scene;
  }
  
  public Collection<Light> getLights ()  {
    return lights;
  }
  
  public int getW () {
    return W;
  }
  
  public int getH () {
    return H;
  }
  
  public Color getBackgroundColor () {
    return background;
  }
  
  public String getTag () {
    return imageName;
  }

  private Transform parseAffineTransformation (final Element element) {

    final TransformBuilder builder = new TransformBuilder();
    
    int args = 0;
    
    final Vector3f translation;
    final NodeList translationNodeList = element.getElementsByTagName("translation");
    if (translationNodeList.getLength() > 0) {
      ++args;
      translation = this.parseVector3f(translationNodeList.item(0).getTextContent());
      builder.setTranslation(translation);
    }
    
    final Vector3f scaleFactors;
    final NodeList scaleFactorsNodeList = element.getElementsByTagName("scaleFactors");
    if (scaleFactorsNodeList.getLength() > 0) {
      ++args;       
      scaleFactors = this.parseVector3f(scaleFactorsNodeList.item(0).getTextContent());
      builder.setScale(scaleFactors);
    }
    
    final Vector3f rotationAxis;
    final NodeList rotationAxisNodeList = element.getElementsByTagName("rotationAxis");
    if (rotationAxisNodeList.getLength() > 0) {
      ++args;      
      rotationAxis = this.parseVector3f(rotationAxisNodeList.item(0).getTextContent());
    } else {
      rotationAxis = new Vector3f(0.0f, 1.0f, 0.0f);
    }

    final float rotationAngle;
    final NodeList rotationAngleNodeList = element.getElementsByTagName("rotationAngle");
    if (rotationAngleNodeList.getLength() > 0) {
      ++args;      
      rotationAngle = Float.parseFloat(rotationAngleNodeList.item(0).getTextContent());
    } else {
      rotationAngle = 0.0f;
    }
    if (signum(rotationAngle) != 0)
      builder.setRotation(rotationAxis, rotationAngle);

    if (parseAimingTransform(element, builder))
      ++args;
    
    return (args > 0) ? builder.build() : IDENTITY_TRANSFORM;
    
  }
  
  private boolean parseAimingTransform (final Element element,
                                        final TransformBuilder builder) {

    final Vector3f vector;
    final Vector3f auxVector;   
    
    final NodeList iVectorNodeList = element.getElementsByTagName("iVector");
    if (iVectorNodeList.getLength() > 0) {    

      vector = this.parseVector3f(iVectorNodeList.item(0).getTextContent());
      
      final NodeList jauxVectorNodeList = element.getElementsByTagName("jauxVector");
      if (jauxVectorNodeList.getLength() > 0) {     
        auxVector = this.parseVector3f(jauxVectorNodeList.item(0).getTextContent());
        builder.setAimingXY(vector, auxVector);
        return true;
      }
      
      final NodeList kauxVectorNodeList = element.getElementsByTagName("kauxVector");
      if (kauxVectorNodeList.getLength() > 0) {         
        auxVector = this.parseVector3f(kauxVectorNodeList.item(0).getTextContent());
        builder.setAimingXZ(vector, auxVector);
        return true;        
      }
      
      throw new IllegalArgumentException("Problema con el vector auxiliar (omitido o incorrecto)");       
      
    } 
    
    final NodeList jVectorNodeList = element.getElementsByTagName("jVector");
    if (jVectorNodeList.getLength() > 0) {    

      vector = this.parseVector3f(jVectorNodeList.item(0).getTextContent());
      
      final NodeList iauxVectorNodeList = element.getElementsByTagName("iauxVector");
      if (iauxVectorNodeList.getLength() > 0) {     
        auxVector = this.parseVector3f(iauxVectorNodeList.item(0).getTextContent());
        builder.setAimingYX(vector, auxVector);
        return true;
      }
      
      final NodeList kauxVectorNodeList = element.getElementsByTagName("kauxVector");
      if (kauxVectorNodeList.getLength() > 0) {         
        auxVector = this.parseVector3f(kauxVectorNodeList.item(0).getTextContent());
        builder.setAimingYZ(vector, auxVector);
        return true;        
      }
      
      throw new IllegalArgumentException("Problema con el vector auxiliar (omitido o incorrecto)");       

    }

    final NodeList kVectorNodeList = element.getElementsByTagName("kVector");
    if (kVectorNodeList.getLength() > 0) {    

      vector = this.parseVector3f(kVectorNodeList.item(0).getTextContent());
      
      final NodeList iauxVectorNodeList = element.getElementsByTagName("iauxVector");
      if (iauxVectorNodeList.getLength() > 0) {     
        auxVector = this.parseVector3f(iauxVectorNodeList.item(0).getTextContent());
        builder.setAimingZX(vector, auxVector);
        return true;
      }
      
      final NodeList jauxVectorNodeList = element.getElementsByTagName("jauxVector");
      if (jauxVectorNodeList.getLength() > 0) {         
        auxVector = this.parseVector3f(jauxVectorNodeList.item(0).getTextContent());
        builder.setAimingZY(vector, auxVector);
        return true;        
      }
      
      throw new IllegalArgumentException("Problema con el vector auxiliar (omitido o incorrecto)");       

    }
    
    return false;

  }

  private VModel parseVisualModel (final Element element) {
    VModel vmodel = null;
    final NodeList materialIdList = element.getElementsByTagName("materialId");
    final NodeList textureFile = element.getElementsByTagName("textureFile");
    if (materialIdList.getLength() > 0) {
      final String materialId = materialIdList.item(0).getTextContent();
      vmodel = this.vmodelCollection.get(materialId);
    } else if (textureFile.getLength() > 0) {
      final String filename = textureFile.item(0).getTextContent();
      // Directorio de búsqueda: ${user.dir}/scenes
      final File file = FileSystems.getDefault().getPath("scenes", filename).toFile();
      //System.out.println(file);
      try {
        vmodel = new VModel(file);
      } catch (final IOException ex) {
        System.err.println("Fichero de textura no encontrado: " + filename);
        System.exit(0);
      }
    } else {
      final NodeList colorList = element.getElementsByTagName("color");
      if (colorList.getLength() > 0) {
        final String colorElementText = colorList.item(0).getTextContent();
        final Color color = parseColor(colorElementText);
        vmodel = new VModel(color);
      }
    }
    return vmodel;
  }
  
  private Shape3D parseSphere (final Element element) {
    
//    final Point3f center = this.parsePoint3f(
//            element.getElementsByTagName("center").item(0).getTextContent());
    final float radius = Float.parseFloat(
            element.getElementsByTagName("radius").item(0).getTextContent());
    
    return new Sphere(radius);

  }
  
  private Shape3D parseBox (final Element element) {

    final float width = Float.parseFloat(
            element.getElementsByTagName("width").item(0).getTextContent());
    final float height = Float.parseFloat(
            element.getElementsByTagName("height").item(0).getTextContent());
    final float depth = Float.parseFloat(
            element.getElementsByTagName("depth").item(0).getTextContent());
    
    return new Box(width, height, depth);
 
  }
  
  private Shape3D parseEllipsoid (final Element element) {
    
    final float a = Float.parseFloat(element.getElementsByTagName("a").item(0).getTextContent());
    final float b = Float.parseFloat(element.getElementsByTagName("b").item(0).getTextContent());
    final float c = Float.parseFloat(element.getElementsByTagName("c").item(0).getTextContent());
    
    return new Ellipsoid(a, b, c);
 
  }
  
  private Shape3D parseGenus2 (final Element element) {
    
    return new Genus2();
 
  }  
  
  private Shape3D parsePlane (final Element element) {
    
    final Point3f Q = this.parsePoint3f(element.getElementsByTagName("point").item(0).getTextContent());
    final Vector3f n = this.parseVector3f(element.getElementsByTagName("normal").item(0).getTextContent());
    
    return new Plane(Q, n);
    
  }  

  private Shape3D parseCylinder (final Element element) {
    
    final float radius = Float.parseFloat(element.getElementsByTagName("radius").item(0).getTextContent());
    final float L = Float.parseFloat(element.getElementsByTagName("Axislength").item(0).getTextContent());
    
    return new Cylinder(radius, L);
    
  }
  
  private Shape3D parseCapsule (final Element element) {
    
    final Point3f A = this.parsePoint3f(element.getElementsByTagName("A").item(0).getTextContent());
    final Point3f B = this.parsePoint3f(element.getElementsByTagName("B").item(0).getTextContent());
    final float r = Float.parseFloat(element.getElementsByTagName("radius").item(0).getTextContent());
    
    return new Capsule(A, B, r);
    
  }
  
  private Shape3D parseTruncatedCone (final Element element) {
    
    final Point3f A = this.parsePoint3f(element.getElementsByTagName("A").item(0).getTextContent());
    final Point3f B = this.parsePoint3f(element.getElementsByTagName("B").item(0).getTextContent());
    final float rA = Float.parseFloat(element.getElementsByTagName("radiusA").item(0).getTextContent());
    final float rB = Float.parseFloat(element.getElementsByTagName("radiusB").item(0).getTextContent());
    
    return new TruncatedCone(A, B, rA, rB);
    
  }    
 
  private Shape3D parseTorus (final Element element) {
 
    final float rotationalRadius = Float.parseFloat(element.getElementsByTagName("rotationalRadius").item(0).getTextContent());
    final float circunferenceRadius = Float.parseFloat(element.getElementsByTagName("circunferenceRadius").item(0).getTextContent());

    return new Torus(rotationalRadius, circunferenceRadius);
    
  }
  
//  private Shape3D parseCyclide (final Element element) {
//    
//    final float a = Float.parseFloat(element.getElementsByTagName("a").item(0).getTextContent());
//    final float b = Float.parseFloat(element.getElementsByTagName("b").item(0).getTextContent());
//    final float d = Float.parseFloat(element.getElementsByTagName("d").item(0).getTextContent());
//    
//    return new Cyclide(a, b, d);
//  
//  }
  private Shape3D parseEgg (final Element element) {
    
    final float a = Float.parseFloat(element.getElementsByTagName("a").item(0).getTextContent());
    final float b = Float.parseFloat(element.getElementsByTagName("b").item(0).getTextContent());
    final float c = Float.parseFloat(element.getElementsByTagName("w").item(0).getTextContent());
    
    return new Egg(a, b, c);
 
  }
  


private String getSubElementText(final Element parent, final String tagName) throws SAXException {
    final Element subElement = (Element) parent.getElementsByTagName(tagName).item(0);
    if (subElement == null || subElement.getTextContent().trim().isEmpty()) {
        throw new SAXException("No se encontró el subelemento: " + tagName);
    }
    return subElement.getTextContent().trim();
}


  
  private Shape3D parsePolygonalMesh (final Element element) throws IOException {
    
    final NodeList flatNodeList = element.getElementsByTagName("flat");
    final boolean flat;
    if (flatNodeList.getLength() > 0) {
      flat = Boolean.parseBoolean(flatNodeList.item(0).getTextContent());
    } else
      flat = false;
    
    final NodeList promNodeList = element.getElementsByTagName("promediatedNormals");
    final boolean promNormals;
    if (promNodeList.getLength() > 0) {
      promNormals = Boolean.parseBoolean(promNodeList.item(0).getTextContent());
    } else
      promNormals = false;    
    
    final ParserOBJ parserobj = new ParserOBJ();
    final String filename = element.getElementsByTagName("objFile").item(0).getTextContent();

    // Directorio de búsqueda: ${user.dir}/scenes
    final File file = FileSystems.getDefault().getPath("scenes", filename).toFile();
    return parserobj.parse(file, flat, promNormals);

  }
  
  static final class TransformBuilder {
      
    static private final String VDON = "Vector director de orientación es nulo";
    static private final String VAON = "Vector auxiliar de orientación es nulo";
    static private final String VOP = "Vectores de orientación son paralelos";
    
    static private final Vector3f I = new Vector3f(1.0f, 0.0f, 0.0f);
    static private final Vector3f J = new Vector3f(0.0f, 1.0f, 0.0f);
    static private final Vector3f K = new Vector3f(0.0f, 0.0f, 1.0f);    
    static private final Vector3f NEUTRAL_S = new Vector3f(1.0f, 1.0f, 1.0f);
    static private final Vector3f NEUTRAL_D = new Vector3f(0.0f, 0.0f, 0.0f);
    static private final Vector3f NEUTRAL_AXIS = new Vector3f(0.0f, 1.0f, 0.0f);
    static private final float NEUTRAL_THETA = 0.0f;
    
    private final Vector3f i;
    private final Vector3f j;
    private final Vector3f k;
    private final Vector3f axis;
    private final Vector3f d;
    private final Tuple3f s;
    private float theta;
    private boolean scaled;
    
    public TransformBuilder () {
      this.i = new Vector3f(I);
      this.j = new Vector3f(J);
      this.k = new Vector3f(K);
      this.axis = new Vector3f(NEUTRAL_AXIS);
      this.d = new Vector3f(NEUTRAL_D);
      this.s = new Vector3f(NEUTRAL_S);
      this.theta = NEUTRAL_THETA;
      this.scaled = false;
    }
    
    TransformBuilder setAimingXY (final Vector3f i, final Vector3f jaux) {
      
      if ((Math.signum(i.x) == 0) &&
          (Math.signum(i.y) == 0) &&
          (Math.signum(i.z) == 0))
        throw new IllegalArgumentException(VDON);
      if ((Math.signum(jaux.x) == 0) &&
          (Math.signum(jaux.y) == 0) &&
          (Math.signum(jaux.z) == 0))
        throw new IllegalArgumentException(VAON);    
      k.cross(i, jaux);
      if (Math.signum(k.dot(k)) == 0)
        throw new IllegalArgumentException(VOP);
      k.normalize();
      this.i.x = i.x;
      this.i.y = i.y;
      this.i.z = i.z;
      this.i.normalize();
      this.j.cross(this.k, this.i); 
      return this;
      
    }
    
    TransformBuilder setAimingXZ (final Vector3f i, final Vector3f kaux) {
      
      if ((Math.signum(i.x) == 0) &&
          (Math.signum(i.y) == 0) &&
          (Math.signum(i.z) == 0))
        throw new IllegalArgumentException(VDON);
      if ((Math.signum(kaux.x) == 0) &&
          (Math.signum(kaux.y) == 0) &&
          (Math.signum(kaux.z) == 0))
        throw new IllegalArgumentException(VAON);
      j.cross(kaux, i);
      if (Math.signum(j.dot(j)) == 0)
        throw new IllegalArgumentException(VOP);
      j.normalize();
      this.i.x = i.x;
      this.i.y = i.y;
      this.i.z = i.z;
      this.i.normalize();
      this.k.cross(this.i, this.j);  
      return this;
      
    }
    
    TransformBuilder setAimingYX (final Vector3f j, final Vector3f iaux) {
      
      if ((Math.signum(j.x) == 0) &&
          (Math.signum(j.y) == 0) &&
          (Math.signum(j.z) == 0))
        throw new IllegalArgumentException(VDON);
      if ((Math.signum(iaux.x) == 0) &&
          (Math.signum(iaux.y) == 0) &&
          (Math.signum(iaux.z) == 0))
        throw new IllegalArgumentException(VAON);    
      k.cross(iaux, j);
      if (Math.signum(k.dot(k)) == 0)
        throw new IllegalArgumentException(VOP);
      k.normalize();
      this.j.x = j.x;
      this.j.y = j.y;
      this.j.z = j.z;
      this.j.normalize();
      this.i.cross(this.j, this.k);
      return this;
      
    }
    
    TransformBuilder setAimingYZ (final Vector3f j, final Vector3f kaux) {
      
      if ((Math.signum(j.x) == 0) &&
          (Math.signum(j.y) == 0) &&
          (Math.signum(j.z) == 0))
        throw new IllegalArgumentException(VDON);
      if ((Math.signum(kaux.x) == 0) &&
          (Math.signum(kaux.y) == 0) &&
          (Math.signum(kaux.z) == 0))
        throw new IllegalArgumentException(VAON);    
      i.cross(j, kaux);
      if (Math.signum(i.dot(i)) == 0)
        throw new IllegalArgumentException(VOP);
      i.normalize();
      this.j.x = j.x;
      this.j.y = j.y;
      this.j.z = j.z;
      this.j.normalize();
      k.cross(this.i, this.j);
      return this;
      
    }    

    TransformBuilder setAimingZX (final Vector3f k, final Vector3f iaux) {
      
      if ((Math.signum(k.x) == 0) &&
          (Math.signum(k.y) == 0) &&
          (Math.signum(k.z) == 0))
        throw new IllegalArgumentException(VDON);
      if ((Math.signum(iaux.x) == 0) &&
          (Math.signum(iaux.y) == 0) &&
          (Math.signum(iaux.z) == 0))
        throw new IllegalArgumentException(VAON);    
      j.cross(k, iaux);
      if (Math.signum(j.dot(j)) == 0)
        throw new IllegalArgumentException(VOP);
      j.normalize();
      this.k.x = k.x;
      this.k.y = k.y;
      this.k.z = k.z;
      this.k.normalize();
      this.i.cross(this.j, this.k);
      return this;
      
    }
    
    TransformBuilder setAimingZY (final Vector3f k, final Vector3f jaux) {
      
      if ((Math.signum(k.x) == 0) &&
          (Math.signum(k.y) == 0) &&
          (Math.signum(k.z) == 0))
        throw new IllegalArgumentException(VDON);
      if ((Math.signum(jaux.x) == 0) &&
          (Math.signum(jaux.y) == 0) &&
          (Math.signum(jaux.z) == 0))
        throw new IllegalArgumentException(VAON);    
      i.cross(jaux, k);
      if (Math.signum(i.dot(i)) == 0)
        throw new IllegalArgumentException(VOP);
      i.normalize();
      this.k.x = k.x;
      this.k.y = k.y;
      this.k.z = k.z;
      this.k.normalize();
      this.j.cross(this.k, this.i);
      return this;
      
    }    
    
    TransformBuilder setRotation (final Vector3f axis, final float theta) {
      if ((Math.signum(axis.x) == 0) &&
          (Math.signum(axis.y) == 0) &&
          (Math.signum(axis.z) == 0))
        throw new IllegalArgumentException("Vector director de eje de rotación nulo");     
      this.axis.x = axis.x;
      this.axis.y = axis.y;
      this.axis.z = axis.z;      
      this.theta = (float) toRadians(theta);
      return this;
    }     
  
    TransformBuilder setTranslation (final Vector3f d) {
      this.d.x = d.x;
      this.d.y = d.y;
      this.d.z = d.z;  
      return this;
    }
    
    TransformBuilder setScale (final Vector3f s) {
      if ((Math.signum(s.x) == 0) ||
          (Math.signum(s.y) == 0) ||
          (Math.signum(s.z) == 0))
        throw new IllegalArgumentException("Factor de escala inválido (igual a 0): " + s);      
      this.s.x = s.x;
      this.s.y = s.y;
      this.s.z = s.z;
      this.scaled = true;
      return this;
    }  
  
    Transform build () {

      final Matrix4f R = Transform.getRotationMatrix(this.axis, this.theta);

      final Matrix4f S = Transform.getScaleMatrix(this.s);

      final Matrix4f T = Transform.getTranslationMatrix(this.d);
      
      final Matrix4f A = Transform.getAimingMatrix(this.i, this.j, this.k);

      return new Transform(S, A, R, T, scaled);

    }
    
  }
  
}