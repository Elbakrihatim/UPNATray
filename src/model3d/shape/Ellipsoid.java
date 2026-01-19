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
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import model3d.boundingvolume.AABB;
import static primitives.ExtendedOperators.opposite;
import raytracer.Hit;
import raytracer.Ray;
//
public final class Ellipsoid extends ProceduralGeometry3D {
  
  static private final Point3f O = new Point3f(0, 0, 0);
  
  private final double a;
  private final double b;
  private final double c;
  private final double a2;
  private final double b2;
  private final double c2;  
  private final float ABC; 
  private final float abc;

  public Ellipsoid (final float a, final float b, final float c) {  
    this.a = 1.0 / a;
    this.b = 1.0 / b;
    this.c = 1.0 / c;
    this.a2 = 1.0 / (a * a);
    this.b2 = 1.0 / (b * b);
    this.c2 = 1.0 / (c * c);
    this.abc = min(a, min(b, c));
    this.ABC = max(a, max(b, c));
    this.boundingBox = new AABB(-a, +a, -b, +b, -c, +c);
  }  
//  @Override
//    protected float SDF(final Point3f P) {
//        final float aux = (float) (P.x * P.x * this.a2 + P.y * P.y * this.b2 + P.z * P.z * this.c2);
//        final float sdf = (float) (Math.sqrt(aux) - 1) * this.abc;
//
//        return sdf;
//    }
  @Override
  protected float SDF (final Point3f P) {

   float xa = (float)((P.x*this.a)*(P.x*this.a));
    float yb = (float)((P.y*this.b)*(P.y*this.b));
    float zc = (float)((P.z*this.c)*(P.z*this.c));
    float fp = xa+yb+zc-1;
    return fp/(2*(float)Math.sqrt((float)((P.x*this.a2)*(P.x*this.a2))+(float)((P.y*this.b2)*(P.y*this.b2))+(float)((P.z*this.c2)*(P.z*this.c2))));
  }
  
  @Override
  public Hit intersect (final Ray ray) {
    final Point3f R = new Point3f(ray.getStartingPoint());
    if (signum(SDF(R)) > 0)
      return intersect(true, ray);
    else if (signum(SDF(R)) < 0)
      return intersect(false, ray);
    else
      return Hit.NOHIT;
  }  
  
  @Override
  public Hit intersectFromOutside (final Ray ray) {
    final Point3f R = new Point3f(ray.getStartingPoint());
    if (signum(SDF(R)) > 0)
      return intersect(true, ray);
    else
      return Hit.NOHIT;
  }

  @Override
  public Hit intersectFromInside (final Ray ray) {
    final Point3f R = new Point3f(ray.getStartingPoint());
    if (signum(SDF(R)) < 0)
      return intersect(false, ray);
    else
      return Hit.NOHIT;
  }    

  @Override
  public Hit intersect (final boolean fromOutside, final Ray ray,
                        final float tmin, final float tmax) {
        if(!fromOutside || (fromOutside && this.boundingBox.intersect(ray, tmin, tmax))){
            final float d = super.rayMarching(ray);
            final float a = abs(d);
            final Point3f P = ray.pointAtParameter(a);
            float xa= (float)(P.x*this.a2);
            float yb= (float)(P.y*this.b2);
            float zc= (float)(P.z*this.c2);
            Vector3f n = new Vector3f(new Point3f(xa, yb, zc));
            n.normalize();
            return (signum(d) > 0) ? new Hit(a, P, n): new Hit(a,P,opposite(n));
        }
        return Hit.NOHIT;
  }
  
  @Override
  protected float distanceUpperBound (final Point3f P) {
    return O.distance(P) + ABC;
  }
  
  @Override
  public boolean isInside (final Point3f _P) {
    final Point3f P = new Point3f(_P);     
    return signum(SDF(P)) < 0;
  }

  @Override
  public boolean isOutside (final Point3f _P) {
    final Point3f P = new Point3f(_P);   
    return signum(SDF(P)) > 0;
  }

  @Override
  public AABB getBoundingBox () {
    return boundingBox;
  }

  @Override
  public boolean intersectAny (final Ray ray, final float tmin, final float tmax) {
    //final Hit hit = intersect(ray, tmin, tmax);
    //return hit.hits();
    if(!isOutside(ray.getStartingPoint()) || (isOutside(ray.getStartingPoint()) && this.boundingBox.intersect(ray, tmin, tmax))){
    //if( this.boundingBox.intersect(ray, tmin, tmax)){
        final float a = abs(super.rayMarching(ray));
        return ((signum(tmin-a) <=0) && (signum(a-tmax) <=0));
    }
    return false;
  }
  
}