package color.densityfunction;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Vector3f;
//
public final class Blinn extends NormalDistributionFunction {
  
  private final float alpha;
  private final float normalizationFactor;
  
  public Blinn (final float beta) {
    super(beta);
    this.alpha = (float) (-LN2 / Math.log(Math.cos(Math.toRadians(beta))));
    this.normalizationFactor = (float) ((alpha + 2) / (2 * Math.PI));
  }
  
  @Override
  public float getProbability (final Vector3f n,
                               final Vector3f w) {
    final float cosTheta  = (float) Math.min(n.dot(w), 1.0);
    if (Math.signum(cosTheta) <= 0)
      return 0.0f;
    return normalizationFactor * (float) Math.pow(cosTheta, alpha);
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