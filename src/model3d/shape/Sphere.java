package model3d.shape;
//

import static java.lang.Math.fma;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import static raytracer.Hit.NOHIT;
import model3d.boundingvolume.AABB;
import static model3d.boundingvolume.AABB.NOBOUNDINGBOX;
import static primitives.ExtendedOperators.opposite;
import raytracer.Hit;
import raytracer.Ray;
//

public class Sphere implements Shape3D {

    static private final Point3f O = new Point3f(0, 0, 0);

    private final float r;
    private final float r2;
    private final float inv_r;

    public Sphere(final float radio) {
        this.r = radio;
        this.r2 = radio * radio;
        this.inv_r = (float) (1.0 / radio);
    }

    @Override
    public Hit intersectFromOutside(final Ray ray, final float tmin, final float tmax) {
        final Vector3f RC = new Vector3f();
        RC.sub(O,ray.getStartingPoint()); //C.sub(R);
        final float c = RC.dot(RC) - r * r;
        if (signum(c) > 0) { // No es necesario si fromOutside es true
            // Punto de partida R fuera de la esfera
            final float b = RC.dot(ray.getDirection());
            if (signum(b - tmin) >= 0 && signum(tmax - b) >= 0) {
           // if (signum(b) > 0) {
                // Centro C de esfera en semiespacio anterior
                final float discr = b * b - c;
                if (signum(discr) > 0) {// Rayo atraviesa a la esfera
                    final float d = (float) Math.sqrt(discr);
                    final float ap = b + d;
                    final float am = c / ap; // am := b - d;
                    final Point3f P = ray.pointAtParameter(am);
                    final Vector3f n = new Vector3f();
                    n.sub(P, O);
                    n.scale(inv_r);
                    return new Hit(am, P, n);
                }
            }
        }
        return NOHIT;
    }
    @Override
    public Hit intersectFromInside(final Ray ray, final float tmin, final float tmax) {
        final Vector3f RC = new Vector3f(); //C.sub(R);
        RC.sub(O, ray.getStartingPoint());
        final float c = RC.dot(RC) - r * r;
        final float b = RC.dot(ray.getDirection());
        final float discr = b * b - c;
        if(discr >= 0)
        {
            final float d = (float) Math.sqrt(discr);
        final float a = b + d;
        if (signum(a - tmin) >= 0 && signum(tmax - a) >= 0) {
             final Point3f P = ray.pointAtParameter(a);
            final Vector3f n = new Vector3f();
            n.sub(P, O); //P.sub(O);
            n.scale(inv_r);
            return new Hit(a, P, opposite(n));
        }
        }
        
        return NOHIT;
    }

    @Override
    public Hit intersect(final boolean fromOutside, final Ray ray) {
        return fromOutside ? intersectFromOutside(ray) : intersectFromInside(ray);
    }

    @Override
    public Hit intersect(final boolean fromOutside, final Ray ray,
            final float tmin, final float tmax) {
        return fromOutside ? intersectFromOutside(ray, tmin, tmax) : intersectFromInside(ray, tmin, tmax);
    }

    @Override
    public boolean isInside(final Point3f P) {
        return Math.signum(signedSquaredDistance(P)) < 0;
    }

    @Override
    public boolean isOutside(final Point3f P) {
        return Math.signum(signedSquaredDistance(P)) > 0;
    }

    private float signedSquaredDistance(final Point3f P) {
        final float x = P.x; // - O.x;
        final float y = P.y; // - O.y;
        final float z = P.z; // - O.z;
        return fma(x, x, fma(y, y, fma(z, z, -r2)));
    }

    @Override
    public AABB getBoundingBox() {
        return NOBOUNDINGBOX;
//        return new AABB(+r, -r, +r, -r, +r, -r);
    }

    @Override
    public boolean intersectAny(final Ray ray, final float tmin, final float tmax) {
        final Vector3f RC = new Vector3f(); //C.sub(R);
        RC.sub(O, ray.getStartingPoint());
        final float c = RC.dot(RC) - r * r;
        final float b = RC.dot(ray.getDirection());
        final float discr = b * b - c;
        final float d = (float) Math.sqrt(discr);
        final float a = b + d;
        return (signum(a - tmin) >= 0 && signum(tmax - a) >= 0);

    }

}
