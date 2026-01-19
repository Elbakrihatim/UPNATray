package view;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import raytracer.RayGenerator;
import raytracer.Ray;
import static primitives.ExtendedOperators.sop;
//
public class NLPerspective extends Projection {
  
  public NLPerspective (final float fov) {
    super(fov, 1.0f);
  }

  @Override
  public RayGenerator getRayGenerator (final Camera c, final int W, final int H) {
    return new NLPerspectiveRayGenerator(c, W, H);
  }

  private final class NLPerspectiveRayGenerator extends RayGenerator {
    
    private final Point3f R;
    private final float h2;

    private NLPerspectiveRayGenerator (final Camera camera,
                                       final int W,
                                       final int H) {
      super(camera, W, H);
      this.R = camera.getPosition();
      this.h2 = 0.25f * h * h;
    }

    @Override
    public Ray getRay (final int m, final int n) {
        /*
          // Convertir coordenadas de píxel a coordenadas normalizadas en el rango [-1, 1]
    float x = (2.0f * m / (W - 1)) - 1.0f;
    float y = 1.0f - (2.0f * n / (H - 1)); // Invertir Y para mantener orientación correcta

    // Calcular la dirección en el espacio de la cámara, usando el campo de visión
    Vector3f direction = new Vector3f(x * w, y * h, -1.0f); // -1 para mirar hacia adelante en el eje z

    // Transformar la dirección al espacio de la escena
    camera.toSceneCoordenates(direction);

    // Crear y retornar el rayo desde la posición de la cámara hacia la dirección calculada
    return new Ray(R, direction);
        */
      return null;

    }

  }

}