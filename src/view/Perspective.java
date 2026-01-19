package view;

/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Point3f;
//
import raytracer.RayGenerator;
import raytracer.Ray;
//

public class Perspective extends Projection {

    public Perspective(final float fov, final float aspect) {
        super((float) (2.0 * Math.tan(Math.toRadians(0.5 * fov))), aspect);
    }

    @Override
    public RayGenerator getRayGenerator(final Camera c, final int W, final int H) {
        return new PerspectiveRayGenerator(c, W, H);
    }

    static private final class PerspectiveRayGenerator extends RayGenerator {

        private final Point3f R;

        private PerspectiveRayGenerator(final Camera c, final int W, final int H) {
            super(c, W, H);
            this.R = camera.getPosition();
        }

        @Override
        public Ray getRay(final int m, final int n) {
            
            final float x = (m) * wW + w2W;
            final float y = (n) * hH + h2H;
            final float z = -1.0f;
            final Point3f Q = new Point3f(x, y, z);
            camera.toSceneCoordenates(Q);

            return new Ray(R, Q);

        }

    }

}
