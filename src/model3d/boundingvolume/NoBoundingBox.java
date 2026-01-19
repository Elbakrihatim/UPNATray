package model3d.boundingvolume;

/**
 *
 * @author MAZ
 */
import java.util.ArrayList;
import java.util.Collection;
import org.jogamp.vecmath.Matrix4f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import raytracer.Ray;
//

final class NoBoundingBox extends AABB {

    NoBoundingBox() {
        super(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    }

    @Override
    public boolean intersect(final Ray ray, final float tmin, final float tmax) {
        return true;
    }

    @Override
    public boolean isOutside(final Point3f P) {
        return false;
    }

    @Override
    public boolean isInside(final Point3f P) {
        return true;
    }

    @Override
    public Collection<Point3f> getCorners() {
        return new ArrayList<>();
    }

    @Override
    public AABB transformedBy(final Matrix4f M) {
        return this;
    }

}
