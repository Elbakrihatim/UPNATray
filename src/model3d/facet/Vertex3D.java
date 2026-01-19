package model3d.facet;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.signum;
//
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.TexCoord2f;
import org.jogamp.vecmath.Vector3f;
//
public final class Vertex3D {
  
  private final Point3f P;
  private Vector3f n;
  private TexCoord2f uv;
  
  public Vertex3D (final Point3f P,
                   final Vector3f n,
                   final TexCoord2f uv) {
    this.P = P;
    this.n = n;
    this.uv = uv;
  }
  
  public Vertex3D (final Point3f P, Vector3f n) {
    this(P, n, null);
  }
  
  public Vertex3D (final Point3f P, final TexCoord2f uv) {
    this(P, null, uv);
  }  
  
  public Vertex3D (final Point3f P) {
    this(P, null, null);
  }    
  
  public Point3f getPoint () { return P; }
  public Vector3f getNormal () { return n; }
  public TexCoord2f getTextureCoordenates () { return uv; }
  
  public void setNormal (final Vector3f n) { this.n = n; }
  public void setTextureCoordenates (final TexCoord2f uv) { this.uv = uv; }
  
  public boolean isXanterior (final float x) { return signum(P.x - x) >= 0; }
  public boolean isYanterior (final float y) { return signum(P.y - y) >= 0; }
  public boolean isZanterior (final float z) { return signum(P.z - z) >= 0; }
  
  public boolean isXposterior (final float x) { return signum(P.x - x) <= 0; }
  public boolean isYposterior (final float y) { return signum(P.y - y) <= 0; }
  public boolean isZposterior (final float z) { return signum(P.z - z) <= 0; }

}
