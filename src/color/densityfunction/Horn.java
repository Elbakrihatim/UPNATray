package color.densityfunction;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Vector3f;
//
public final class Horn extends NormalDistributionFunction {
  
  private final float q;
  
  public Horn (final float q) {
    super(0);
    this.q = q;
  }
  
  @Override
  public float getProbability (final Vector3f n,
                               final Vector3f w) {
    final float cosTheta = Math.min(1.0f, n.dot(w));
    if (Math.signum(cosTheta) < 0)
      return 0;
    final float cosA = 2 * (cosTheta * cosTheta) - 1;
    return (float) Math.pow(cosA, q);
  }  

  @Override
  public float shadowing(Vector3f out) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Vector3f getSample() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setFrame(Vector3f out, Vector3f n) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public float getAlpha() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public float getGFactor(float vh, float vn) {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

}