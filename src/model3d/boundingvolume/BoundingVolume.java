package model3d.boundingvolume;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Matrix4f;
//
import model3d.Volumetric;
import raytracer.Ray;
//
public interface BoundingVolume extends Volumetric {
 
  /* 
    Devuelve true si el rayo atraviesa el volumen
    definido por la envoltura; false en caso contrario.
  */
  public boolean intersect (final Ray ray, final float tmin, final float tmax);
  
  /* 
    Devuelve un array con los valores del parámetro alfa de los puntos
    de intersección del rayo con las fronteras del volumen; los valores
    del array están ordenados en creciente.
  */  
  public float[] intersectComplete (final Ray ray);
  
  /*
    Devuelve el volumen envolvente resulta de aplicar la transformación afín
    cuya matriz se proporciona como argumento.
  */
  public BoundingVolume transformedBy (final Matrix4f M); 
  
  
}

