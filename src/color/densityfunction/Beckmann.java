package color.densityfunction;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Vector3f;
//
public final class Beckmann extends NormalDistributionFunction {
 
  private final float alpha2;
  private final float cn;
  
  public Beckmann (final float beta) {
    super(beta);
    final double cosBeta = Math.cos(this.beta);
    final double tanBeta = Math.tan(this.beta);
    this.alpha2 = (float) ((tanBeta * tanBeta) / (LN2 - 4 * Math.log(cosBeta)));
    this.cn = (float) (Math.PI * alpha2);
  }
  
  @Override
  public float getProbability (final Vector3f n,
                               final Vector3f w) {
    final float cosTheta  = (float) Math.min(n.dot(w), 1.0);
    if (Math.signum(cosTheta) <= 0)
      return 0;
    final float cosTheta2 = cosTheta * cosTheta;
    final float cosTheta4 = cosTheta2 * cosTheta2;
    final float xp = (cosTheta2 - 1) / (alpha2 * cosTheta2);
    final float nm = (float) Math.exp(xp);
    final float dn = cn * cosTheta4;
    return nm / dn;
  }
  
  @Override
  public float getGFactor (final float vh, final float vn) {
    final float x = vh / vn;
    if (Math.signum(x) <= 0)
      return 0;
    else if (Math.signum(x - 1.6f) >= 0)
      return 1;
    else //if (Math.signum(x - 1.6f) <= 0)
      return x * (3.535f + 2.181f * x) / (1 + x * (2.276f + x * 2.577f));
  }
  

  @Override
  public float shadowing (Vector3f out) {
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
  
}