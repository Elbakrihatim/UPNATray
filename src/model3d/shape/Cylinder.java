package model3d.shape;
/**
 *
 * @author MAZ
 */
import model3d.boundingvolume.AABB;
import static model3d.boundingvolume.AABB.NOBOUNDINGBOX;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import static primitives.ExtendedOperators.opposite;
import static primitives.ExtendedOperators.triple;
import raytracer.Hit;
import raytracer.Ray;
//
public final class Cylinder implements Shape3D {
  
  static private final Vector3f AUX_U = new Vector3f(1.0f, 0.0f, 0.0f);    
  static private final Vector3f AUX_W = new Vector3f(0.0f, 0.0f, 1.0f);   

  private final float halfL;
  private final float r;
  private final float r2;
  private final float rinv;
  
  private final AABB boundingBox;

  public Cylinder (final float r,
                   final float L) {
    this.halfL = L * 0.5f;
    this.r = r;
    this.r2 = r * r;
    this.rinv = 1 / r;
    this.boundingBox = NOBOUNDINGBOX;  // Hay que proporcionar una AABB concreta.
  }


  @Override
  public Hit intersect(boolean fromOutide, Ray ray) {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

  @Override
  public boolean isInside(Point3f P) {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

  @Override
  public boolean isOutside(Point3f P) {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

  @Override
  public AABB getBoundingBox() {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

    @Override
    public Hit intersect(boolean fromOutside, Ray ray, float tmin, float tmax) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean intersectAny(Ray ray, float tmin, float tmax) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
  
}
