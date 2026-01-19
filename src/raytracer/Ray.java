package raytracer;
//

import static java.lang.Math.fma;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//

public class Ray {

    public static final float SHIFT_DISTANCE = 0.005f;

    private final Point3f R;
    private Vector3f v;
    private final float vnorm;
    private float eta;

    public Ray(final Point3f R, final Vector3f v) {
        this.R = new Point3f(R);
        this.v = new Vector3f(v);
        this.v.normalize();
        final double vnorm2 = v.dot(v);
        this.vnorm = (float) Math.sqrt(vnorm2);
    }

    public Ray(final Point3f R, final Point3f Q) {
        this.R = new Point3f(R);
        this.v = new Vector3f();
        this.v.sub(Q, R);
        this.v.normalize();
        final double vnorm2 = v.dot(v);
        this.vnorm = (float) Math.sqrt(vnorm2);
    }

    /**
     * Constructor copia
     *
     * @param ray
     */
    public Ray(final Ray ray) {
        this(ray.getStartingPoint(), ray.getDirection());
    }

    public Vector3f getDirection() {
        return this.v;
    }

    public Point3f getStartingPoint() {
        return this.R;
    }

    public void setDirection(final Vector3f v) {
        this.v = v;
    }

    public Point3f pointAtParameter(final float t) {
        final Point3f Q = new Point3f();
        Q.scaleAdd(t, v, R);
        return Q;
    }

    public float getEvilSeed() {
        return vnorm;
    }

    public void shift() {
        R.x = fma(SHIFT_DISTANCE, v.x, R.x); // R.y += SHIFT_DISTANCE * v.y;
        R.y = fma(SHIFT_DISTANCE, v.y, R.y); // R.y += SHIFT_DISTANCE * v.y;
        R.z = fma(SHIFT_DISTANCE, v.z, R.z); // R.z += SHIFT_DISTANCE * v.z;
    }

    public boolean isOperative() {
        return (Math.signum(this.getEvilSeed() - 0.5E-7f) > 0);
    }

    public void setMediumRefractionIndex(final float eta) {
        this.eta = eta;
    }

    public float getMediumRefractionIndex() {
        return eta;
    }

    @Override
    public String toString() {
        return "Rayo: origen " + R.toString() + " direccion " + v.toString();
    }

}
