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
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import static raytracer.Hit.NOHIT;
import model3d.boundingvolume.AABB;
import static model3d.boundingvolume.AABB.NOBOUNDINGBOX;
import raytracer.Hit;
import raytracer.Ray;
//
public final class Box implements Shape3D {

  private final float halfW;
  private final float halfH;
  private final float halfD;
  
  private final AABB boundingBox;
  
  public Box (final float w, final float h, final float d) {
    this.halfW = 0.5f * w;
    this.halfH = 0.5f * h;
    this.halfD = 0.5f * d;
    this.boundingBox = NOBOUNDINGBOX;
  }
 
  @Override
  public Hit intersectFromOutside (final Ray ray) {
    return intersect(true, ray);
  }

  @Override
  public Hit intersectFromInside (final Ray ray) {
    return intersect(false, ray);
  }
  
  @Override
  public Hit intersect (final boolean fromOutside, final Ray ray,
                        final float tmin, final float tmax) {
    
    final Point3f  R = ray.getStartingPoint();
    final Vector3f v = ray.getDirection();
    
    // Cómputo de intersección de un rayo con una caja en posición canónica
       if (signum(v.x) != 0) { // Rayo no paralelo a las caras
            final float invx =-1 / v.x;
            final float al = (R.x + halfW) * invx;
            final float ar = (R.x- halfW) * invx;
            final float a = fromOutside? min(al, ar) : max(al,ar);
            if (signum(a) > 0) { // Intersección en semiespacio anterior
                final float dy = halfH- abs(R.y + a * v.y);
                final float dz = halfD- abs(R.z + a * v.z);
                if ((signum(dy) >= 0) && (signum(dz) >= 0)) {
                    // Intersección dentro de los límites de la cara
                    final Point3f P = ray.pointAtParameter(a);
                    //final Vector3f n =
                    //(signum(v.x) < 0) ? new Vector3f(i) : opposite(i);
                    final Vector3f n = (signum(v.x) < 0) ? new Vector3f(1, 0, 0) : new Vector3f(-1, 0, 0);
                    return new Hit(a, P, n);
                }
            }
        }
       
       if (signum(v.y) != 0) { // Rayo no paralelo a las caras en el eje Y
        final float invy = -1 / v.y;
        final float bt = (R.y + halfH) * invy;
        final float bb = (R.y - halfH) * invy;
        final float b = fromOutside? min(bt, bb) : max(bt,bb);
        if (signum(b) > 0) { // Intersección en rango
            final float dx = halfW - abs(R.x + b * v.x);
            final float dz = halfD - abs(R.z + b * v.z);
            if ((signum(dx) >= 0) && (signum(dz) >= 0)) {
                // Intersección dentro de los límites de la cara en Y
                final Point3f P = ray.pointAtParameter(b);
                final Vector3f n = (signum(v.y) < 0) ? new Vector3f(0, 1, 0) : new Vector3f(0, -1, 0);
                return new Hit(b, P, n);
            }
        }
    }

    // Comprobar intersección en el eje Z
    if (signum(v.z) != 0) { // Rayo no paralelo a las caras en el eje Z
        final float invz = -1 / v.z;
        final float cf = (R.z + halfD) * invz;
        final float cb = (R.z - halfD) * invz;
        final float c = fromOutside? min(cf, cb) : max(cf, cb);
        if (signum(c) > 0) { // Intersección en rango
            final float dx = halfW - abs(R.x + c * v.x);
            final float dy = halfH - abs(R.y + c * v.y);
            if ((signum(dx) >= 0) && (signum(dy) >= 0)) {
                // Intersección dentro de los límites de la cara en Z
                final Point3f P = ray.pointAtParameter(c);
                final Vector3f n = (signum(v.z) < 0) ? new Vector3f(0, 0, 1) : new Vector3f(0, 0, -1);
                return new Hit(c, P, n);
            }
        }
    }
       
    return NOHIT;
    
  }

  @Override
  public boolean isOutside (final Point3f P) {
    // P está dentro de la caja orientada con los ejes si, en cada dimensión,
    // el valor absoluto de la diferencia entre las correspondiente coordenadas
    // de P y el punto central C es menor que la mitad de la longitud de la caja
    // en esa dimensión.
    return (Math.abs(P.x) > halfW) || (Math.abs(P.y) > halfH) || (Math.abs(P.z) > halfD);
    //return false;
  }

  @Override
  public boolean isInside (final Point3f P) {
    // P está fuera de la caja orientada con los ejes si, en elguna dimensión,
    // el valor absoluto de la diferencia entre las correspondiente coordenadas
    // de P y el punto central C es mayor que la mitad de la longitud de la caja
    // en esa dimensión.      
    return (Math.abs(P.x) <= halfW) && (Math.abs(P.y) <= halfH) && (Math.abs(P.z) <= halfD);
    //return false;
  }
  
  
  @Override
  public AABB getBoundingBox () {
    return new AABB(-halfW, +halfW, -halfH, +halfH, -halfD, +halfD);
  }  

  @Override
  public boolean intersectAny (final Ray ray, final float tmin, final float tmax) {
    
    final Point3f  R = ray.getStartingPoint();
    final Vector3f v = ray.getDirection();

    // Debe determinarse si el la rayo intersecta con la caja en el rango dado.

    //return false;
    // Inicializamos los intervalos máximos y mínimos de intersección
    float tNear = tmin;
    float tFar = tmax;

    // Intersección en el eje X
    if (v.x != 0) {
        float tx1 = (-halfW - R.x) / v.x;
        float tx2 = (halfW - R.x) / v.x;
        float tminX = Math.min(tx1, tx2);
        float tmaxX = Math.max(tx1, tx2);
        tNear = Math.max(tNear, tminX);
        tFar = Math.min(tFar, tmaxX);
        if (tNear > tFar) return false; // No hay intersección
    } else if (Math.abs(R.x) > halfW) {
        return false; // Rayo paralelo y fuera de los límites de la caja en X
    }

    // Intersección en el eje Y
    if (v.y != 0) {
        float ty1 = (-halfH - R.y) / v.y;
        float ty2 = (halfH - R.y) / v.y;
        float tminY = Math.min(ty1, ty2);
        float tmaxY = Math.max(ty1, ty2);
        tNear = Math.max(tNear, tminY);
        tFar = Math.min(tFar, tmaxY);
        if (tNear > tFar) return false; // No hay intersección
    } else if (Math.abs(R.y) > halfH) {
        return false; // Rayo paralelo y fuera de los límites de la caja en Y
    }

    // Intersección en el eje Z
    if (v.z != 0) {
        float tz1 = (-halfD - R.z) / v.z;
        float tz2 = (halfD - R.z) / v.z;
        float tminZ = Math.min(tz1, tz2);
        float tmaxZ = Math.max(tz1, tz2);
        tNear = Math.max(tNear, tminZ);
        tFar = Math.min(tFar, tmaxZ);
        if (tNear > tFar) return false; // No hay intersección
    } else if (Math.abs(R.z) > halfD) {
        return false; // Rayo paralelo y fuera de los límites de la caja en Z
    }

    // Si tNear <= tFar, hay una intersección dentro del rango
    return tNear <= tFar;
}
}