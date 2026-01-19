package model3d.shape;

/**
 *
 * @author MAZ
 */
import static java.lang.Math.abs;
import static java.lang.Math.signum;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import static model3d.boundingvolume.AABB.NOBOUNDINGBOX;
import static raytracer.Hit.NOHIT;
import static primitives.ExtendedOperators.opposite;
import model3d.boundingvolume.AABB;
import raytracer.Hit;
import raytracer.Ray;
//

public final class Plane implements Shape3D {

    private final Point3f Q;
    private final Vector3f n;

    public Plane(final Point3f Q, final Vector3f n) {
        this.Q = new Point3f(Q);
        this.n = new Vector3f(n);
        this.n.normalize();
    }

    public Plane(final Point3f A, final Point3f B, final Point3f C) {
        this.Q = new Point3f(A);
        final Vector3f AB = new Vector3f();
        AB.sub(B, A);
        final Vector3f AC = new Vector3f();
        AC.sub(C, A);
        this.n = new Vector3f();
        this.n.cross(AB, AC);
        this.n.normalize();
    }

    @Override
    public Hit intersect(boolean fromOutide, Ray ray) {
        return intersect(ray);
    }

    @Override
    public boolean isInside(final Point3f P) {
        // Se devuelve true si P no pertenece al plano. 
        /*final Vector3f PQ = new Vector3f();
        PQ.sub(Q, P);
        return (signum(abs(PQ.dot(n)) - 1E-5f) >= 0);
    */
        return false;
    }

    @Override
    public boolean isOutside(final Point3f P) {
        // Se devuelve true si P no pertenece al plano.    
        final Vector3f PQ = new Vector3f();
        PQ.sub(Q, P);
        return (signum(abs(PQ.dot(n)) - 1E-5f) >= 0);
    }

    @Override
    public AABB getBoundingBox() {
        return NOBOUNDINGBOX;
    }

    @Override
    public Hit intersect(final boolean fromOutside, Ray ray, float tmin, float tmax) {

        // Cómputo de intersección de un rayo con un plano
        final float vn = ray.getDirection().dot(n);
        if (intersectAny(ray,tmin,tmax))
        {
            final Vector3f RQ = new Vector3f();
            RQ.sub(Q, ray.getStartingPoint());
            final float RQn = RQ.dot(n);
            final float a = RQn / vn;
            //Punto de intersección en semiespacio anterior
            final Point3f P = ray.pointAtParameter(a);
            if (signum(RQn) < 0) {
                // Normal hacia punto R
                return new Hit(a, P, new Vector3f(n));
            } else if (signum(RQn) > 0) {
                // Normal hacia punto R
                return new Hit(a, P, opposite(n));
            }
        }
        return NOHIT;
    }

    @Override
    public boolean intersectAny(final Ray ray, final float tmin, final float tmax) {

        // Cómputo de intersección de un rayo con un plano
        final float vn = ray.getDirection().dot(n);
        if (signum(vn) != 0) {
            // Rayo no paralelo al plano
            final Vector3f RQ = new Vector3f();
            RQ.sub(Q, ray.getStartingPoint());

            final float RQn = RQ.dot(n);
            final float a = RQn / vn;
            return (signum(a - tmin) >= 0 && signum(tmax - a) >= 0);
        }
        return false;

    }

}
