package model3d.facet;
/**
 *
 * @author MAZ
 */
import java.util.Collection;
//
import raytracer.Hit;
import raytracer.Ray;
//
public interface Facet {
  
  Hit intersect (final boolean fromOutside, final Ray ray,
                 final float tmin, final float tmax);
  
  boolean intersectAny (final Ray ray, final float tmin, final float tmax);  

  public Collection<Vertex3D> getVertices ();
  
  public boolean isXanterior (final float x);
  public boolean isYanterior (final float y);
  public boolean isZanterior (final float z);
  public boolean isXposterior (final float x);
  public boolean isYposterior (final float y);
  public boolean isZposterior (final float z);
  
  // Para indicar si se renderiza con suavizado de normales.
  public void setFlat ();

}
