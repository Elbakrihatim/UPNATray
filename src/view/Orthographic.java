package view;

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import raytracer.RayGenerator;
import raytracer.Ray;
//

public class Orthographic extends Projection {

    public Orthographic(final float h, final float aspect) {
        super(h, aspect);
    }

    @Override
    public RayGenerator getRayGenerator(final Camera c, final int W, final int H) {
        return new OrthographicRayGenerator(c, W, H);
    }

    static private final class OrthographicRayGenerator extends RayGenerator {

        private final Vector3f v;

        private OrthographicRayGenerator(final Camera c, final int W, final int H) {
            super(c, W, H);
            this.v = camera.getLook();
        }

        @Override
        public Ray getRay(final int m, final int n) {

            final float x = m * wW + w2W;
            final float y = n * hH + h2H;
            final float z = 0.0f;
            final Point3f R = new Point3f(x, y, z);
            camera.toSceneCoordenates(R);

            return new Ray(R, v);
        }

    }

}
