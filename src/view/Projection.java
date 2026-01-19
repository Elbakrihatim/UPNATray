package view;
/**
 *
 * @author MAZ
 */
import raytracer.RayGenerator;
//
public abstract class Projection {

  private final float width;  // Anchura ventana de proyección
  private final float height; // Altura ventana de proyección

  protected Projection (final float height, final float aspect) {
    if (height <= 0)
      throw new IllegalArgumentException("Altura de la ventana de proyección debe ser estrictamente positiva");
    if (height <= 0)
      throw new IllegalArgumentException("Relación de aspecto debe ser estrictamente positiva");
    this.height = height;
    this.width  = height * aspect;
  }  
  
  public final float getWidth () {
    return width;
  }

  public final float getHeight () {
    return height;
  }

  public abstract RayGenerator getRayGenerator (final Camera c, final int W, final int H);

}