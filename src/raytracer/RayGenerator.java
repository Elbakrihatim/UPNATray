package raytracer;
//
import java.util.Random;
import view.Camera;
//
public abstract class RayGenerator {
  
  final protected Random rg;
	
  final protected Camera camera; // Cámara para la que se generan los rayos primarios
  final protected float w; // Anchura de la ventana de proyección
  final protected float h; // Altura de la ventana de proyección
  final protected int W; // Número de columnas de la imagen raster
  final protected int H; // Número de filas de la imagen raster
  final protected float wW; // Relación entre w y W
  final protected float hH; // Relación entre h y H
  final protected float w2W; // (1 / 2) * (w / W - w)
  final protected float h2H; // (1 / 2) * (h / H - h)

  protected RayGenerator (final Camera c, final int W, final int H) {

    this.camera = new Camera(c);
    this.w = c.getProjection().getWidth();
    this.h = c.getProjection().getHeight();
    this.W = W;
    this.H = H;
    this.wW = w / W;
    this.hH = h / H;
    this.w2W = 0.5f * (wW - w);
    this.h2H = 0.5f * (hH - h);
    
    this.rg = new Random();
    this.rg.setSeed(System.nanoTime());

  }

  /**
   *
   * @param m
   * @param n
   * @return
   */
  public abstract Ray getRay (final int m, final int n);
	
}