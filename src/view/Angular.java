package view;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import raytracer.RayGenerator;
import raytracer.Ray;
//
public class Angular extends Projection {

  public Angular (final float omega) {
    super((float) (2.0 * sin(toRadians(omega * 0.5f))), 1);
  }
  
  @Override
  public RayGenerator getRayGenerator (final Camera c, final int W, final int H) {
    return new AngularRayGenerator(c, W, H);
  }

  static private final class AngularRayGenerator extends RayGenerator {

    private final float w2;  // Cuadrado del radio de la imagen 
    private final float cos; // Coseno de omega / 2
    private final Point3f R;

    private AngularRayGenerator (final Camera c, final int W, final int H) {
      super(c, W, H);
      this.w2 = w * w * 0.25f;
      this.cos = (float) sqrt(1.0 - w2);
      this.R = new Point3f(0.0f, 0.0f, cos);      
      camera.toSceneCoordenates(R);
    }

    @Override
    public Ray getRay (final int m, final int n) {
        float x = (m * wW) + w2W;
        float y = (n * hH) + h2H;
        float z = (float) (Math.toRadians((Math.cos(w/2.0f))) - (float)Math.sqrt(1.0f - (x * x + y * y)));
        //float z = (float) (this.cos - (float)Math.sqrt(1.0f - (x * x + y * y)));
        Point3f point = new Point3f(x, y, z);
        camera.toSceneCoordenates(point);
        return new Ray(R, point);

    }



  }

}