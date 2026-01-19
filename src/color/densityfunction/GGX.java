package color.densityfunction;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.fma;
import static java.lang.Math.signum;
import org.jogamp.vecmath.Vector3f;
//
public class GGX extends NormalDistributionFunction {
  
  static private final float SQRT2 = (float) Math.sqrt(2);

  private final float alpha;  
  private final float alpha2;
  private final float cn;  
  private Vector3f i;
  private Vector3f j;
  private Vector3f k;
  
  public GGX (final float beta) {
    super(beta);
    final double cosBeta = Math.cos(this.beta);
    final double cosBeta2 = cosBeta * cosBeta;
    this.alpha2 = (float) ((1 - cosBeta2) / (SQRT2 - cosBeta2));
    this.cn = (float) (alpha2 / Math.PI);
    this.alpha = (float) Math.sqrt(alpha2);
  }
  
  @Override
  public float getProbability (final Vector3f n,
                               final Vector3f w) {
    final float ca = (float) Math.min(n.dot(w), 1.0);
    if (Math.signum(ca) <= 0) {
      //System.out.println(Math.acos(ca) / Math.PI);
      return 0;
    }
    final float dn = fma(ca * ca, (alpha2 - 1), 1);
//    if (signum(cn / (dn * dn) - 69) > 0)
//      System.out.println(dn + " " + ca + " " + cn);
    return cn / (dn * dn);
  }
  
  @Override
  public float getGFactor (final float vh, final float vn) {
    final float x = vh / vn;
    if (Math.signum(x) <= 0)
      return 0;
    else {
      final float tg2 = 1 / (vn * vn) - 1;
      final float dn = 1 + (float) Math.sqrt(1 + alpha2 * tg2);
      return 2 / dn;
    }
  }  

  @Override
  public float shadowing (final Vector3f out) {
    final float x = out.x;
    final float y = out.y;
    final float z = out.z;
    
    final float t0 = alpha2 * (x * x + z * z);
    final float t1 = (float) ((Math.sqrt(1 + t0 / (y * y)) - 1) * 0.5);
    
    return 1.0f / (1.0f + t1);
  }

  @Override
  public Vector3f getSample () {
    final double u1 = rg.nextDouble();
    final double u2 = rg.nextDouble();
    
    final double r   = Math.sqrt(u1);
    final double phi = 2.0 * Math.PI * u2;
    
    final double s  = 0.5 * (1.0 + k.y);
    final double t1 = r * Math.cos(phi);
    final double t2 = (1.0 - s) * Math.sqrt(1.0 - t1 * t1) + s * r * Math.sin(phi);
    final double t3 = Math.sqrt(Math.max(0.0, 1.0 - t1 * t1 - t2 * t2));
    
    final Vector3f Nh = new Vector3f();
    Nh.scaleAdd((float) t1, i, Nh);
    Nh.scaleAdd((float) t2, j, Nh);
    Nh.scaleAdd((float) t3, k, Nh);
    
    final Vector3f N = new Vector3f(alpha2 * Nh.x, Math.max(0.0f, Nh.y), alpha2 * Nh.z);
    N.normalize();
    
    return N;
  }

  @Override
  public void setFrame (final Vector3f out, final Vector3f n) {
    k = new Vector3f(alpha2 * out.x, out.y, alpha2 * out.z);
    k.normalize();
    final Vector3f _j = new Vector3f(0, 1, 0);
    final Vector3f _i = new Vector3f();
    _i.cross(_j, k);
    _i.normalize();
    final float i2 = _i.dot(_i);
    i = (Math.signum(i2) > 0) ? _i : new Vector3f(1, 0, 0);
    j = new Vector3f();
    j.cross(k, i);
  }

  @Override
  public float getAlpha () {
    return alpha;
  }
  
}