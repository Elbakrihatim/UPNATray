package model3d.shape;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.abs;
import static java.lang.Math.fma;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import org.jogamp.vecmath.Matrix3f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import static raytracer.Hit.NOHIT;
import model3d.boundingvolume.AABB;
import raytracer.Hit;
import raytracer.Ray;
//
public final class TruncatedCone extends ProceduralGeometry3D {
 
  private final Point3f A;
  private final Point3f B;
  private final Vector3f axis; // B - A
  private final float rA;
  private final float rB;  
  private final float L;
  private final float slope;
  private final float cosa;
  private final float sena;

  public TruncatedCone (final Point3f A, final Point3f B,
                        final float rA, final float rB) {
    
    if (signum(rA) < 0)
      throw new IllegalArgumentException("Valor de radio illegal: " + rA);
    if (signum(rB) < 0)
      throw new IllegalArgumentException("Valor de radio illegal: " + rB);    
    
    if (signum(rB - rA) >= 0) {
      this.A = new Point3f(A);
      this.B = new Point3f(B);
      this.rA = rA;
      this.rB = rB;      
    } else {
      this.A = new Point3f(B);
      this.B = new Point3f(A);
      this.rA = rB;
      this.rB = rA;
    }
    
    this.axis = new Vector3f();
    this.axis.sub(this.B, this.A);
    this.axis.normalize(); // B - A  normalizado
    
    this.L = A.distance(B);
    this.slope = (this.rB - this.rA) / L;
    this.cosa = (float) sqrt(1 / fma(slope, slope, 1));
    this.sena = (float) sqrt(-fma(cosa, cosa, -1));
    
  }

  @Override
  protected float SDF (final Point3f P) {
    return Float.POSITIVE_INFINITY;
  }

  @Override
  public Hit intersectFromOutside (final Ray ray) {
    if (signum(SDF(ray.getStartingPoint())) > 0)
      return intersect(true, ray);
    else
      return NOHIT;
  }

  @Override
  public Hit intersectFromInside (final Ray ray) {
    if (signum(SDF(ray.getStartingPoint())) < 0)
      return intersect(false, ray);
    else
      return NOHIT;    
  }  

  @Override
  public Hit intersect (final boolean fromOutside, final Ray ray,
                        final float tmin, final float tmax) {
    
    return NOHIT;
    
  }
  
  @Override
  protected float distanceUpperBound (final Point3f P) {
    return max(A.distance(P) + rA, B.distance(P) + rB);
  }

  @Override
  public boolean intersectAny (Ray ray, float tmin, float tmax) {
    final Hit hit = intersect(ray, tmin, tmax);
    return hit.hits();
  }
  
}