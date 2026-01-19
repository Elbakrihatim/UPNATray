package scene;
/**
 * Colección de objetos que forman la escena
 * 
 * @author MAZ
 */
import color.VModel;
import java.util.LinkedHashMap;
import java.util.Map;
//
import raytracer.Hit;
import raytracer.Ray;
import model3d.Intersectable;
import model3d.Model3D;
import model3d.group3d.Group3D;
//
public class Scene implements Intersectable {
  
  private final Map<Model3D, VModel> vmap;

  public Scene () {
    this.vmap = new LinkedHashMap<>();
  }

  public Scene addObject (final Model3D model3d, final VModel vmodel) {
    vmap.put(model3d, vmodel);  
    return this;
  }

  @Override
  public Hit intersect (final Ray ray, final float tmin, final float tmax) {

    Hit closestHit = Hit.NOHIT;

    for (final Model3D objeto: vmap.keySet()) {
      
      final Hit lastHit = objeto.intersect(ray, tmin, tmax);

      if (lastHit.isCloserThan(closestHit)) {
        lastHit.setModel3D(objeto);
        lastHit.setVisualModel(vmap.get(objeto));
        closestHit = lastHit;
      }

    }

    return closestHit;

  }
  
  public Map<Model3D, VModel> getVMap () {
    return vmap;
  }
  
  public Group3D getObjects () {
    return new Group3D(vmap.keySet());
  }  

  @Override
  public boolean intersectAny (Ray ray, float tmin, float tmax) {
    
    for (final Model3D objeto: vmap.keySet()) {
      
      if (objeto.intersectAny(ray, tmin, tmax))
        return true;

    }
    
    return false;
    
  }

}