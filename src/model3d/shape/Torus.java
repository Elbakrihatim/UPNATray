package model3d.shape;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.abs;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import static raytracer.Hit.NOHIT;
import model3d.boundingvolume.AABB;
import raytracer.Hit;
import raytracer.Ray;
import static primitives.ExtendedOperators.sop;
//
public final class Torus extends ProceduralGeometry3D {
  
  static private final Point3f O = new Point3f(0, 0, 0);

  private final float L;
  private final float r;
  private final float inv_r;

  public Torus () {
    this(2.0f, 1.0f);
  }
  
  public Torus (final float L, final float r) {
    
    if (signum(r) <= 0)
      throw new IllegalArgumentException("Valor de radio r ilegal: " + r);
    
   if (signum(r) < 0)
      throw new IllegalArgumentException("Valor de radio L ilegal: " + L);    

    this.L = L;
    this.r = r;
    this.inv_r = 1 / r;
    this.boundingBox =  new AABB(-(L + r), +(L + r),
                                 -r, +r,
                                 -(L + r), +(L + r));

  }
  
  @Override
  public Hit intersect (final Ray ray) {
    final Point3f R = new Point3f(ray.getStartingPoint());
    if (signum(SDF(R)) > 0)
      return intersect(true, ray);
    else if (signum(SDF(R)) < 0)
      return intersect(false, ray);
    else
      return NOHIT;
  }  

  @Override
  public Hit intersect (final boolean fromOutside, final Ray ray,
                        final float tmin, final float tmax) { 
    if(intersectAny(ray,tmin,tmax)){
        final float t = abs(super.rayMarching(ray)); // Distancia desde el rayo hasta el toro
        final Point3f P = new Point3f(ray.getStartingPoint());
        Vector3f direction = new Vector3f(ray.getDirection());
        direction.scale(t); // Escalar la dirección por la distancia t
        P.add(direction);   // P (punto de interseccion) = startingPoint + t * direction
        Vector3f u = new Vector3f(P.x, 0, P.z); 
        u.normalize();
        Point3f C = new Point3f(u);
        C.scale(L);
        C.add(O); 
        Vector3f CP = new Vector3f(P);
        CP.sub(C); 
        CP.normalize();
        if(!fromOutside)
            CP.negate();
        return new Hit(abs(super.rayMarching(ray)), P, CP);
    }
    return NOHIT;

  }
  @Override
   protected float SDF(final Point3f P) {
        final float delta = (float) Math.sqrt(P.x * P.x + P.z * P.z) - L;
        return (float) Math.sqrt(delta * delta + P.y * P.y) - r;
   }
  
  @Override
  protected float distanceUpperBound (final Point3f P) {
    return O.distance(P) + L + r;
  }
  
  @Override
  public boolean isInside (final Point3f P) {    
    return signum(SDF(P)) < 0;
  }

  @Override
  public boolean isOutside (final Point3f P) {   
    return signum(SDF(P)) > 0;
  }

  @Override
  public AABB getBoundingBox() {
    return boundingBox;
  }

  @Override
  public boolean intersectAny (final Ray ray, final float tmin, final float tmax) {
    //final Hit hit = intersect(ray, tmin, tmax);
    //return hit.hits();
    if(!isOutside(ray.getStartingPoint()) || ( isOutside(ray.getStartingPoint()) && this.getBoundingBox().intersect(ray, tmin, tmax)))
    {
        final float t = abs(super.rayMarching(ray));
        return (signum(t - tmin) >= 0 && signum(tmax - t) >= 0);
    }
    return false;
  }
  
}