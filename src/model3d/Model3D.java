package model3d;

/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Point3f;
//
import static raytracer.Hit.NOHIT;
import model3d.boundingvolume.AABB;
import model3d.shape.Shape3D;
import raytracer.Hit;
import raytracer.Ray;
//

public class Model3D implements Intersectable, Volumetric {

    private final VolumetricIntersectable volume;
    private final AABB boundingBox;
    private final Transform transform;

    public Model3D(final Shape3D shape,
            final Transform transform) {
        this.volume = shape;
        // Cada modelo geométrico debe ser capaz de devolver una bounding box
        // que lo envuelva. Con el objetivo de mejorar la eficiencia, hay que
        // considerar sobreescribir la implementación por defecto del método
        // getBoundingBox() dada en la clase Shape.
        this.boundingBox = transform.modelToWorld(shape.getBoundingBox());
        this.transform = new Transform(transform);
    }

    @Override
    public boolean isOutside(final Point3f P) {
        if (!boundingBox.isOutside(P)) {
            final Point3f _P = new Point3f(P);
            transform.worldToModel(_P);
            return volume.isOutside(_P);
        } else {
            return true;
        }
    }

    @Override
    public boolean isInside(final Point3f P) {
        if (boundingBox.isInside(P)) {
            final Point3f _P = new Point3f(P);
            transform.worldToModel(_P);
            return volume.isInside(_P);
        } else {
            return false;
        }
    }

    @Override
    public Hit intersect(final Ray ray, final float tmin, final float tmax) {

        if (boundingBox.intersect(ray, tmin, tmax)) {
            final Ray transformedRay = transform.worldToModel(ray);
            final Hit hit = volume.intersect(transformedRay, tmin, tmax);
            if (hit.hits()) {
                return transform.modelToWorld(hit, ray.getStartingPoint());
            }
        }

        return NOHIT;

    }

    @Override
    public boolean intersectAny(final Ray ray, final float tmin, final float tmax) {

        if (boundingBox.intersect(ray, tmin, tmax)) {
            final Ray transformedRay = transform.worldToModel(ray);
            return volume.intersectAny(transformedRay, tmin, tmax);
        }

        return false;

    }

    public Volumetric getVolume() {
        return volume;
    }

}
