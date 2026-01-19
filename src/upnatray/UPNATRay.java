package upnatray;
//
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
//
import engine.GraphicEngine;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import light.Light;
import parser.Parser;
import raytracer.RayTracer;
import scene.Scene;
//
public final class UPNATRay {
  
  /**
   * Main.
   *
   * @param args
   * @throws IOException
   */
  public static void main (final String[] args) {//throws IOException {

    // Instante inicial
    final float t0 = System.nanoTime();

    // Especificación de escena y vista
    
    // Directorio donde buscar ficheros de escena: ${user.dir}/scenes
    final FileSystem fileSystem = FileSystems.getDefault();
    final Path pathToFile = fileSystem.getPath("scenes", args[0]);
    final File inputFile = pathToFile.toFile();    
    final Parser parser;
    try {
      parser = new Parser(inputFile);
    } catch (final FileNotFoundException ex) {
      System.err.println("Fichero " + inputFile.getName() + " no encontrado");
      return;
    } catch (final IOException ex) {
      System.err.println("Problema de lectura con fichero " + inputFile.getName());
      return;      
    } catch (final ParserConfigurationException | SAXException ex) {
      System.err.println("Problema de parseo con fichero " + inputFile.getName());
      System.err.println(ex);
      return;       
    }
    
    final String tag = parser.getTag();
    final int W = parser.getW();
    final int H = parser.getH();
    final Color background = parser.getBackgroundColor();
    final Scene scene = parser.getScene();
    final Collection<Light> lights = parser.getLights();
    final GraphicEngine engine = parser.getEngine();
    final RayTracer rayTracer = parser.getRayTracer();    
   
    // Preparada la escena 
    final long t1 = System.nanoTime();
    final float buildingTime = (float) ((t1 - t0) * 1E-9);
    
    System.out.printf("Tiempo para construir la escena: %4.2f segundos", buildingTime);
    
    final BufferedImage image = (!lights.isEmpty()) 
            ? rayTracer.synthesis(W, H, engine, scene, lights, background)
            : rayTracer.synthesis(W, H, engine, scene, background);

    final long t2 = System.nanoTime();
    final float renderingTime = (float) ((t2 - t1) * 1E-9);  

    System.out.printf("Tiempo de renderizado: %4.2f segundos", renderingTime);    
    
    if (args.length > 1) {
      try {
        final Path pathToOutputFile = fileSystem.getPath("images", args[1]);
        ImageIO.write(image, "jpg", pathToOutputFile.toFile());
      } catch (final IOException ex) {
        System.err.println("Problema de escritra con fichero de salida " + inputFile.getName());    
      }
    }
    
    // Se muestra la imagen sintetizada.
    show(image, tag);
    
  }
  
  static private void show (final BufferedImage image, final String tag) { 
    
    final JFrame frame = new JFrame(tag);
    
    frame.getContentPane().setLayout(new FlowLayout());
    frame.getContentPane().add(new JLabel(new ImageIcon(image)));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.repaint();
    frame.setVisible(true);
    
  }

}