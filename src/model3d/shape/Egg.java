package model3d.shape;

import static java.lang.Math.*;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import model3d.boundingvolume.AABB;
import static primitives.ExtendedOperators.dop;
import static primitives.ExtendedOperators.opposite;
import raytracer.Hit;
import raytracer.Ray;

public final class Egg extends ProceduralGeometry3D {

    static private final Point3f O = new Point3f(0, 0, 0);

    private final double a;
    private final double b;
    private final double w;
    private final double a2;
    private final double c2;
    private final double b2;

    public Egg(final float a, final float b, final float w) {
        if (a <= 0 || b <= 0 || w <= 0 || b <= w) {
            throw new IllegalArgumentException("Parámetros inválidos para el huevo: a=" + a + ", b=" + b + ", w=" + w);
        }

        this.a = a;
        this.b = b;
        this.w = w;
        this.a2 = a * a;
        this.b2 = b * b;
        this.c2=w*w;
        this.boundingBox = new AABB(-a, +a, -b, +b, -a, +a); // Ajuste de la bounding box
    }

    @Override
protected float SDF(Point3f P) {
    // Calculamos los cuadrados de las coordenadas
    float x2 = P.x * P.x;
    float y2 = P.y * P.y;
    float z2 = P.z * P.z;
    float aux = (float) ((2.0f * P.y + w) * w + b2);
    float fP = (float) ((x2 * aux) + (y2 * a2) + dop(z2, aux, a2, b2));
    float gradX = P.x * aux;
    float gradY = (float) (P.y * a2 + w * (x2 + z2));
    float gradZ = P.z * aux;
    float modGradfP = 2.0f * (float) sqrt(gradX * gradX + gradY * gradY + gradZ * gradZ);
    return fP / modGradfP;
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
            Vector3f n = new Vector3f(new Point3f(P.x * (float)fma(fma(2, P.y, w), w, b2),(float) fma(P.x * P.x, w, fma(P.y, a2, (float)(P.z * P.z)*w)),P.z * (float)fma(fma(2, P.y, w), w, b2)));
            n.normalize();
            return (signum(d) > 0) ? new Hit(a, P, n): new Hit(a,P,opposite(n));
        }
        return Hit.NOHIT;
  }
  
  @Override
  protected float distanceUpperBound (final Point3f P) {
    return O.distance(P);
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
    if(!isOutside(ray.getStartingPoint()) || (isOutside(ray.getStartingPoint()) && this.boundingBox.intersect(ray, tmin, tmax))){
        final float a = abs(super.rayMarching(ray));
        return ((signum(tmin-a) <=0) && (signum(a-tmax) <=0));
    }
    return false;
  }
}
