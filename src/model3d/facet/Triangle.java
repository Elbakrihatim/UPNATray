package model3d.facet;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.fma;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.TexCoord2f;
import org.jogamp.vecmath.Vector3f;
//
import static raytracer.Hit.NOHIT;
import raytracer.Hit;
import raytracer.Ray;
import static primitives.ExtendedOperators.dop;
import static primitives.ExtendedOperators.sop;
//
public class Triangle implements Facet {
  
  private final Vertex3D A;
  private final Vertex3D B;
  private final Vertex3D C;
  private final Vector3f g; // Normal geométrica no normalizada
  private final Vector3f AB;
  private final Vector3f AC; 
  
  public final float a00;
  public final float a11;
  public final float a01;
  
  public final float den; 
  
  private final boolean noTextured;  

  private float area;

  public Triangle (final Vertex3D A, final Vertex3D B, final Vertex3D C,
                   final boolean isFlat) {

    this.A = A;
    this.B = B;
    this.C = C;
    this.AB = new Vector3f();
    this.AB.sub(B.getPoint(), A.getPoint()); // B - A
    this.AC = new Vector3f();
    this.AC.sub(C.getPoint(), A.getPoint()); // C - A
    this.g = new Vector3f();
    this.g.cross(AB, AC);
    
    this.a00 = AB.dot(AB);
    this.a11 = AC.dot(AC);
    this.a01 = AB.dot(AC);
    this.den = 1 / dop(a00, a11, a01, a01);     
    
    this.noTextured = (A.getTextureCoordenates() == null) ||
                      (B.getTextureCoordenates() == null) ||
                      (C.getTextureCoordenates() == null);

    // Área: mitad del módulo de la norma geométrica.
    this.area = (float) (0.5 * sqrt(g.dot(g)));

  }
  
  public Triangle (final Point3f A, final Point3f B, final Point3f C) {
    this(new Vertex3D(A), new Vertex3D(B), new Vertex3D(C), true);
  }  

  public Triangle (final Vertex3D A, final Vertex3D B, final Vertex3D C) {
    this(A, B, C, false);
  }

  @Override
  public Hit intersect (final boolean fromOutside,
                        final Ray ray, final float tmin, final float tmax) { // el método verifica si un rayo intersect el triángulo definido por los vértices A,B y C y 
      // si es así entonces calcula la posición exacta de la intersección en el triángulo y devuelve un objeto Hit.
    final Point3f R = ray.getStartingPoint(); // punto de inicio del rayo
    final Vector3f v = ray.getDirection(); // dirección del rayo
    final float c =- v.dot(g); // es el ángulo relativo entre la dirección del rayo y la normal geométrica del triángulo. Si c es positiva el rayo se dirige hacia el triángulo sino se aleja.
    if (fromOutside ? signum(c) > 0 : signum(c) < 0) { // si fromOutside es true entonces el rayo debe venir de "fuera" del triágulo c>0
        final Vector3f AR = new Vector3f(R);
        AR.sub(A.getPoint());
        final float b = AR.dot(g); // mide la posición del origen del rayo respecto al plano del triángulo. Si b > 0 entonces el rayo está del lado "frontal" del plano definido por el triángulo. Sino está en el "semiespacio posterior"
        if(fromOutside? signum(b) >0 : signum(b)<0){ 
            final float a = b / c; //calcula la distancia a lo largo del rayo donde ocurre la intersección.
            if ((signum(tmin- a) <= 0) && (signum(a- tmax) <= 0)) {
                final Point3f P = ray.pointAtParameter(a); // punto exacto de intersección.
                final Vector3f AP = new Vector3f(P);
                AP.sub(A.getPoint());
                //final float invdet = 1 / dop(u, w, v, v);
                final float s1 = AP.dot(this.AB);
                final float s2 =  AP.dot(this.AC);
                final float beta = dop(s1, a11, s2, a01) * den;
                if ((signum(beta) >= 0) && (signum(beta- 1) <= 0)) {
                    //final float gamma = dop(s2, u, s1, v) * invdet;
                    final float gamma = dop(s2, a00, s1, a01) * den;
                    if ((signum(gamma) >= 0) && (signum(gamma- 1) <= 0)) {
                        final float w = 1- beta- gamma;
                        if (signum(w) >= 0) {
                            final Vector3f n = smoothNormal(fromOutside,w,beta,gamma);
                            
                            return new Hit(a,P,n);
                        }
                    }
                }
            }
        }
    }
    return NOHIT;
  }  

  
  private Vector3f smoothNormal (final boolean fromOutside,
                                 final float aa, final float bb, final float cc) {
    // Se computa el vector normal a devolver: aa * n_A + bb * n_B + cc * cn_C
    // El vector debe apuntar al semiespacio donde se encuentra el punto R.
    final Vector3f n = new Vector3f();
    n.scaleAdd(aa,A.getNormal(),n);
    n.scaleAdd(bb,B.getNormal(),n);
    n.scaleAdd(cc,C.getNormal(),n);
    n.normalize();
    return n;
  }
 
  public final Vector3f getNormal () {
    return this.g;
  }

  public final Point3f getA () {
    return this.A.getPoint();
  }

  public final Point3f getB () {
    return this.B.getPoint();
  }

  public final Point3f getC () {
    return this.C.getPoint();
  }

  public float area () {
    return area;
  }

  @Override
  public boolean isXanterior (final float x) {
    return A.isXanterior(x) || B.isXanterior(x) || C.isXanterior(x);
  }

  @Override
  public boolean isYanterior (final float y) {
    return A.isYanterior(y) || B.isYanterior(y) || C.isYanterior(y);
  }

  @Override
  public boolean isZanterior (final float z) {
    return A.isZanterior(z) || B.isZanterior(z) || C.isZanterior(z);
  }

  @Override
  public boolean isXposterior (final float x) {
    return A.isXposterior(x) || B.isXposterior(x) || C.isXposterior(x);
  }

  @Override
  public boolean isYposterior (final float y) {
    return A.isYposterior(y) || B.isYposterior(y) || C.isYposterior(y);
  }

  @Override
  public boolean isZposterior (final float z) {
    return A.isZposterior(z) || B.isZposterior(z) || C.isZposterior(z);
  }

//  @Override
  public boolean isAnterior (final Point3f P) {
    final float x = P.x - A.getPoint().x;
    final float y = P.y - A.getPoint().y;
    final float z = P.z - A.getPoint().z;
    final float b = fma(x, g.x, fma(y, g.y, z * g.z));
    return Math.signum(b) > 0;
  }

//  @Override
  public boolean isPosterior (final Point3f P) {
    final float x = P.x - A.getPoint().x;
    final float y = P.y - A.getPoint().y;
    final float z = P.z - A.getPoint().z;
    final float b = fma(x, g.x, fma(y, g.y, z * g.z));
    return Math.signum(b) < 0;
  }

  @Override
  public Collection<Vertex3D> getVertices () {
    final List<Vertex3D> list = new ArrayList<>(3);
    list.add(A);
    list.add(B);
    list.add(C);
    return list;
  }

  @Override
  public void setFlat () { area = (signum(area) > 0) ? -area : area; }


  @Override
    public boolean intersectAny(final Ray ray, final float tmin, final float tmax) {
        final Point3f R = ray.getStartingPoint();
        final Vector3f v = ray.getDirection();
        final float c = -v.dot(g);
        // Verifica que el rayo esté orientado hacia el triángulo
        if (c <= 0) return false;
        // Cálculo de la distancia de intersección potencial
        final Vector3f AR = new Vector3f(R);
        AR.sub(A.getPoint());
        final float b = AR.dot(g);
        if(b <= 0) return false;
        final float a = b / c;
        // Verifica que `a` esté dentro del rango tmin y tmax
        if (a < tmin || a > tmax) return false;
        // Calcular punto de intersección baricéntrica y verificar límites
        final Point3f P = ray.pointAtParameter(a);
        final Vector3f AP = new Vector3f(P);
        AP.sub(A.getPoint());
        final float s1 = AP.dot(this.AB);
        final float s2 = AP.dot(this.AC);
        final float beta = dop(s1, a11, s2, a01) * den;
        if (beta < 0 || beta > 1) return false;
        final float gamma = dop(s2, a00, s1, a01) * den;
        if (gamma < 0 || gamma > 1) return false;
        final float w = 1 - beta - gamma;
        if (w < 0) return false;
        return true; // Intersección encontrada
    }  
}