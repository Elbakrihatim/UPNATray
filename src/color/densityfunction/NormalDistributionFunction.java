package color.densityfunction;
/**
 *
 * @author MAZ
 */
import java.util.Random;
import org.jogamp.vecmath.Matrix3f;
import org.jogamp.vecmath.Vector3f;
//
public abstract class NormalDistributionFunction {
  
  static protected final double LN2 = Math.log(2);
  
  protected final float beta;
  protected final Random rg;
  protected final Matrix3f T;  
  
  NormalDistributionFunction (final float beta) {
    if ((Math.signum(beta) < 0) || (Math.signum(beta - 90) > 0))
      throw new IllegalArgumentException("beta debe estar en el intervalo [0,90]");    
    this.beta = (float) Math.toRadians(beta);
    this.rg = new Random();
    rg.setSeed(System.nanoTime());
    this.T = new Matrix3f();
  }
  
  public abstract float getProbability (final Vector3f n,
                                        final Vector3f w);
  
  public abstract float getGFactor (final float vh, final float vn);
  
  public abstract float shadowing (final Vector3f out);
  
  public abstract Vector3f getSample ();
  
  public abstract void setFrame (final Vector3f out, final Vector3f n);
  
  public abstract float getAlpha ();
  
}