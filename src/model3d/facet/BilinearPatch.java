package model3d.facet;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.abs;
import static java.lang.Math.fma;
import static java.lang.Math.signum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static javax.swing.text.html.HTML.Tag.U;
import model3d.boundingvolume.AABB;
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
public final class BilinearPatch implements Facet {
  
  static private final BilinearFunctionsNewtonSolver SOLVER
    = new BilinearFunctionsNewtonSolver();
  
  static {
    System.out.println(BilinearPatch.class.getCanonicalName());
  }

  private final Vertex3D A;
  private final Vertex3D B;
  private final Vertex3D C;
  private final Vertex3D D;

  private final Vector3f AB;
  private final Vector3f AD;
  private final Vector3f E;
  
  // Normal geomÃ©trica promediada
  private final Vector3f g;

  private final boolean isRhomboid;

  private float area;

  private final float e2;
  private final float e3;
  private final float e4;
  
  private final float f2;
  private final float f3;
  private final float f4;
  
  private final AABB boundingBox;
  
  public BilinearPatch (final Vertex3D A,
                        final Vertex3D B,
                        final Vertex3D C,
                        final Vertex3D D) {
    this.A = A;
    this.B = B;
    this.C = C;
    this.D = D;
    this.AB = new Vector3f();
    this.AB.sub(B.getPoint(), A.getPoint());
    this.AD = new Vector3f();
    this.AD.sub(D.getPoint(), A.getPoint());
    this.E = new Vector3f();
    //this.E.sub(AD, AB);
    this.E.sub(this.C.getPoint(),this.D.getPoint());
    this.E.sub(AB);
    // Si los vectores (B - A) y (C - D) son paralelos y de igual magnitud
    // entonces el vector E es nulo.
    // Si el vector E es nulo, entonces el parche bilineal es un romboide.
    this.isRhomboid = (signum(E.length() - 0.5E-6f) <= 0);

    if (!isRhomboid) {
        // Calcular y asignar la AABB para el parche
        List<Point3f> points = new ArrayList<>();
        points.add(this.A.getPoint());
        points.add(this.B.getPoint());
        points.add(this.C.getPoint());
        points.add(this.D.getPoint());
        this.boundingBox = new AABB(points);
    } else {
        this.boundingBox = null; // Asignar null si es un romboide
    }
    
    // CÃ³mputo de la normal geomÃ©trica.
    final Vector3f AD = new Vector3f();
    AD.sub(D.getPoint(), A.getPoint());
    this.g = new Vector3f();
    if (isRhomboid) {
      this.g.cross(AB, AD);
      this.area = g.length();
    } else {  // Normal geomÃ©Ã©Ã©trica promediada
      final Vector3f n = new Vector3f();

      n.cross(AB, AD);
      g.add(n);
      
      final Vector3f DC = new Vector3f();
      DC.sub(C.getPoint(), D.getPoint()); // C - D     
      
      n.cross(DC, AD);
      g.add(n);

      final Vector3f AC = new Vector3f();
      AC.sub(C.getPoint(), A.getPoint()); // C - A

      n.cross(AB, AC);
      g.add(n);

      final Vector3f BD = new Vector3f();
      BD.sub(D.getPoint(), B.getPoint()); // D - B

      n.cross(DC, BD);
      g.add(n);

      g.normalize();

      this.area = 1;
    }

    // TÃ©rminos para primera funciÃ³n/ecuaciÃ³n:
    this.e2 = AB.dot(AB);
    this.e3 = this.AD.dot(AB);
    this.e4 = E.dot(AB);
    // TÃ©rminos para primera funciÃ³n/ecuaciÃ³n:
    this.f2 = e3; // AB.dot(DC);
    this.f3 = this.AD.dot(this.AD);
    this.f4 = E.dot(this.AD);

  }


  @Override
public Hit intersect (final boolean fromOutside, final Ray ray, final float tin, final float tout) {
  return isRhomboid ? intersectWithRhomboid(fromOutside, ray, tin, tout)
                                     : boundingBox.intersect(ray, tin, tout) ? intersectWithQuadratic(fromOutside, ray, tin, tout) : NOHIT;
}
 
private Hit intersectWithRhomboid (final boolean fromOutside, final Ray ray,
                                     final float tmin, final float tmax) {
    final Point3f R = ray.getStartingPoint(); 
    final Vector3f v = ray.getDirection();
    final float c =- v.dot(g); 
    if (fromOutside ? signum(c) > 0 : signum(c) < 0) { 
        final Vector3f AR = new Vector3f(R);
        AR.sub(A.getPoint());
        final float b = AR.dot(g); 
        if(fromOutside? signum(b) >0 : signum(b)<0){ 
            final float a = b / c; 
            if ((signum(tmin- a) <= 0) && (signum(a- tmax) <= 0)) {
                final Point3f P = ray.pointAtParameter(a); 
                final Vector3f AP = new Vector3f(P);
                AP.sub(A.getPoint());
                final float s1 = AP.dot(this.AB);
                final float s2 =  AP.dot(this.AD);
                final float det = dop(this.e2,this.AD.dot(this.AD),this.AB.dot(this.AD),this.AB.dot(this.AD));
                if(det == 0)
                    return NOHIT;
                final float invdet = 1/det;
                final float beta = dop(s1, this.AD.dot(this.AD), s2, this.AB.dot(this.AD)) * invdet;
                if ((signum(beta) >= 0) && (signum(beta- 1) <= 0)) {
                    final float gamma = dop(s2, this.e2, s1, this.AB.dot(this.AD)) * invdet;
                    if ((signum(gamma) >= 0) && (signum(gamma- 1) <= 0)) {
                            Vector3f normal = getNormal(this.e2, this.AB.dot(this.AD), (fromOutside ? +1 : -1));
                            return new Hit(a,P,normal);
                    }
                }
            }
        }
    }
    return NOHIT;
    
  } 


  
  private Hit intersectWithQuadratic(boolean fromOutside, Ray ray, float tmin, float tmax) {
    Point3f R = ray.getStartingPoint();
    Vector3f v = ray.getDirection();
    Vector3f RA = new Vector3f();
    RA.sub(A.getPoint(), R);
    
    float alpha1 = RA.dot(v);
    float alpha2 = AB.dot(v);
    float alpha3 = AD.dot(v);
    float alpha4 = E.dot(v);
    
    float e0 = v.dot(AB);
    float e1 = RA.dot(AB);
    float e2 = AB.dot(AB);
    float e3 = AD.dot(AB);
    float e4 = E.dot(AB);
    
    float f0 = v.dot(AD);
    float f1 = RA.dot(AD);
    float f2 = AB.dot(AD);
    float f3 = AD.dot(AD);
    float f4 = E.dot(AD);
    
    float a1 = fma(e0, alpha1, -e1); 
    float a2 = fma(e0, alpha2, -e2);
    float a3 = fma(e0, alpha3, -e3);
    float a4 = fma(e0, alpha4, -e4);
    float b1 = fma(f0, alpha1, -f1);
    float b2 = fma(f0, alpha2, -f2);
    float b3 = fma(f0, alpha3, -f3);
    float b4 = fma(f0, alpha4, -f4);
    
    float[] betaGamma = SOLVER.solve(a1, a2, a3, a4, b1, b2, b3, b4);
    if (betaGamma.length == 0) {
        return NOHIT;
    }
    float beta = betaGamma[0];
    float gamma = betaGamma[1];
    
    if (beta < 0 || beta > 1 || gamma < 0 || gamma > 1) {
        return NOHIT;
    }

    float alpha = fma(fma(beta, alpha4, alpha3), gamma, fma(beta, alpha2, alpha1));
    if (alpha < tmin || alpha > tmax || alpha < 0) {
        return NOHIT;
    }
    
    Point3f intersection = new Point3f(); 
    intersection.scaleAdd(alpha, v, R);
    Vector3f normal = getNormal(beta, gamma, (fromOutside ? -1 : 1) *signum(g.dot(v)));
    return new Hit(alpha, intersection, normal);
}

  
  private Vector3f getNormal (final float U, final float V,
                              final float sense) {
    final Vector3f n;
    if (signum(area) > 0) {
      final Vector3f nAB = new Vector3f();
      nAB.scaleAdd(1 - U, A.getNormal(), nAB);
      nAB.scaleAdd(U, B.getNormal(), nAB);
      final Vector3f nDC = new Vector3f();
      nDC.scaleAdd(1 - U, D.getNormal(), nDC);
      nDC.scaleAdd(U, C.getNormal(), nDC);
      n = new Vector3f();
      n.scaleAdd(sense * (1 - V), nAB, n);
      n.scaleAdd(sense * V, nDC, n);
      n.normalize(); 
    } else {
      n = new Vector3f(g);
      n.scale(-sense / area);
    }
    return n;
  }

  @Override
  public boolean isXanterior (final float x) {
    return A.isXanterior(x) ||
           B.isXanterior(x) ||
           C.isXanterior(x) || 
           D.isXanterior(x);
  }

  @Override
  public boolean isYanterior (final float y) {
    return A.isYanterior(y) ||
           B.isYanterior(y) ||
           C.isYanterior(y) || 
           D.isYanterior(y);
  }

  @Override
  public boolean isZanterior (final float z) {
    return A.isZanterior(z) ||
           B.isZanterior(z) ||
           C.isZanterior(z) || 
           D.isZanterior(z);
  }

  @Override
  public boolean isXposterior (final float x) {
    return A.isXposterior(x) ||
           B.isXposterior(x) ||
           C.isXposterior(x) ||
           D.isXposterior(x);
  }

  @Override
  public boolean isYposterior (final float y) {
    return A.isYposterior(y) ||
           B.isYposterior(y) ||
           C.isYposterior(y) ||
           D.isYposterior(y);
  }

  @Override
  public boolean isZposterior (final float z) {
    return A.isZposterior(z) ||
           B.isZposterior(z) ||
           C.isZposterior(z) ||
           D.isZposterior(z);
  }

  @Override
  public Collection<Vertex3D> getVertices () {
    final List<Vertex3D> list = new ArrayList<>(4);
    list.add(A);
    list.add(B);
    list.add(C);
    list.add(D);
    return list;
  }

  @Override
  public void setFlat () { area = (signum(area) > 0) ? -area : area; }

  @Override
  public boolean intersectAny (Ray ray, float tmin, float tmax) {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }
 
}
