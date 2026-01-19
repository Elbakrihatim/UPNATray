package raytracer;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.floor;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import org.jogamp.vecmath.Vector3f;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
//
import engine.GraphicEngine;
import light.Light;
import scene.Scene;
import view.Camera;
//
public class RayTracer {

  static private final int WBLOCK = 8;
  static private final int HBLOCK = 8;

  private final int hblock;
  private final int wblock;
  private boolean progress;
  private final Camera camera;
  
  public RayTracer (final int wblock,
                    final int hblock,
                    final Camera camera,
                    final boolean progress) {
    this.hblock = hblock;
    this.wblock = wblock;  
    this.camera = camera;
    this.progress = progress;
  }
  
  public RayTracer (final int wblock,
                    final int hblock,
                    final Camera camera) {
    this(wblock, hblock, camera, true);
  }   

  public RayTracer (final Camera camera, final boolean progress) {
    this(WBLOCK, HBLOCK, camera, progress);
  }

  public RayTracer (final Camera camera) {
    this(WBLOCK, HBLOCK, camera, true);
  }

  public BufferedImage synthesis (final int W, final int H,
                                  final GraphicEngine engine,
                                  final Scene scene,
                                  final Color background) {
      return _synthesis_(W, H, engine, scene, null, background);
  }

  public BufferedImage synthesis (final int W, final int H,
                                  final GraphicEngine engine,
                                  final Scene scene,
                                  final Collection<Light> lights,
                                  final Color background) {
    return _synthesis_(W, H, engine, scene, lights, background);
  }

  private BufferedImage _synthesis_ (final int W, final int H,
                                     final GraphicEngine engine,
                                     final Scene scene,
                                     final Collection<Light> lights,
                                     final Color background) {

    final BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

    final JFrame frame;
    if (progress) {
      frame = new JFrame("in progress ...");
      frame.getContentPane().setLayout(new FlowLayout());
      frame.getContentPane().add(new JLabel(new ImageIcon(image)));
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
    } else
      frame = null;

    final RayGenerator rayGenerator = camera.getRayGenerator(W, H);
    final Vector3f viewVector = camera.getLook();

    final int pw = W / wblock;
    final int ph = H / hblock;

    for (int j = 0; j < pw; ++j) {

      for (int k = 0; k < ph; ++k) {

        final int _m = (int) floor((j + 1) * W / pw);
        for (int m = (int) floor(j * W / pw); m < _m; ++m) {

          final int _n = (int) floor((k + 1) * H / ph);
          for (int n = (int) floor(k * H / ph); n < _n; ++n) {

            final Ray ray = rayGenerator.getRay(m, n);

            if (ray.isOperative()) {

              final Hit hit = scene.intersect(ray);

              if (hit.hits()) {
//                  Si este método devuelve que sí significa que un rayo ha intersectado con un pixel
//                  por lo que se puede colorear
                
                final Color color = engine.getColor(viewVector, scene, lights, hit, ray);
                //final Vector3f v = ray.getDirection();
                //final Color color = engine.getColor(hit, v); //viewVector);
                putPixel(image, m, n, color);

              } else {
                putPixel(image, m, n, background);
              }

            } else {
              putPixel(image, m, n, Color.WHITE);
            }

          }

        }
        
        if (progress)
          frame.repaint();        

      }

    }

    if (progress)
      frame.dispose();

    return image;

  }

  private void putPixel (final BufferedImage image,
                         final int m, final int n, final Color c) {
    image.setRGB(m, image.getHeight() - 1 - n, c.getRGB());
  }

  public void setProgressOn () {
    this.progress = true;
  }

  public void setProgressOff () {
    this.progress = false;
  }

}