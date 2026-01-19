package color;
/**
 *
 * @author MAZ
 */
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import org.jogamp.vecmath.TexCoord2f;
//
public final class VModel {
  
  static private final Random RG = new Random(System.nanoTime()); 
  
  private final Color color;
  private final BufferedImage texture;
  private final BSDF bsdf;
  
  private VModel (final Color color,
                  final BSDF bsdf,
                  final BufferedImage texture) {
    this.color = color;
    this.bsdf = bsdf;
    this.texture = texture;
  }
  
  public VModel () {
    this(randomColor(), null, null);
  }
  
  public VModel (final Color color) {
    this(color, null, null);
  }
  
  public VModel (final BSDF bsdf) {
    this(null, bsdf, null);
  }  

  public VModel (final File file) throws IOException {
    this(null, null, ImageIO.read(file));
  }
  
  public Color getColor (final TexCoord2f uv) {
    if (texture != null) {
      final float u = uv.x;
      final float v = uv.y;
      try {
        return new Color(texture.getRGB((int) (u * texture.getWidth()),
                                        (int) (v * texture.getHeight())));
      } catch (final java.lang.ArrayIndexOutOfBoundsException ex) {
        System.out.println("Coordenada u: " + " " + u + " (" + (u * texture.getWidth()) + ")");
        System.out.println("Coordenada v: " + " " + v + " (" + (v * texture.getHeight()));
        throw ex;
      }
    } else
      return getColor();
  }
      
  public Color getColor () {
    if (color != null) {
      return color;
    } else {
      return randomColor();
    }
  }
  
  public BSDF getBSDF () {
    return bsdf;  
  }
  
  static private Color randomColor () {
    final int r = RG.nextInt(256);
    final int g = RG.nextInt(256);
    final int b = RG.nextInt(256);
    return new Color(r, g, b);
  }
  
}