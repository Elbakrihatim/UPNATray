package color.densityfunction;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Vector3f;
//
public final class Torrance extends NormalDistributionFunction {
  
  static private final double SQRTLOG2 = Math.sqrt(LN2);

  private final double alpha2;
  
  public Torrance (final float beta) {
    super(beta);    
    final double b = SQRTLOG2 / beta;
    this.alpha2 = b * b;
  }
  
  @Override
  public float getProbability (final Vector3f n,
                               final Vector3f w) { 
    final float cosTheta = w.dot(n);
    if (Math.signum(cosTheta) < 0)
      return 0;    
    final double theta = Math.acos(cosTheta);
    final double exponente = alpha2 * (theta * theta);
    return (float) Math.exp(-exponente);
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