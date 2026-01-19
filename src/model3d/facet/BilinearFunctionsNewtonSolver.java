package model3d.facet;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.abs;
import static java.lang.Math.fma;
import static java.lang.Math.signum;
//
import static primitives.ExtendedOperators.dop;
import static primitives.ExtendedOperators.sop;
//
final class BilinearFunctionsNewtonSolver {

  // NÃºmero mÃ¡ximo de iteraciones por defecto
  static private final int _N = 5;
  
  // Valor del parÃ¡metro para estimar la mejora en la aproximaciÃ³n computada
  static private final float RHO =
    (System.getProperty("RHO") != null) ?
    Float.parseFloat(System.getProperty("RHO")) : 1E-3f;

  private final float rho2;
  private final int N;

  BilinearFunctionsNewtonSolver () {
    this.rho2 = RHO * RHO;
    this.N = _N;
  }
  
  BilinearFunctionsNewtonSolver (final float rho, final int N) {
    this.rho2 = rho * rho;
    this.N = N;
  }

 float[] solve(float a1, float a2, float a3, float a4, float b1, float b2, float b3, float b4) {
    float u = 0.5f, v = 0.5f; 
    for (int k = 0; k < N; k++) {
        float dFdu = Math.fma(a4, v, a2); 
        float dFdv = Math.fma(a4, u, a3); 
        float dGdu = Math.fma(b4, v, b2); 
        float dGdv = Math.fma(b4, u, b3); 
        float F = eval(a1, a2, a3, a4, u, v);
        float G = eval(b1, b2, b3, b4, u, v);
        
        float det = dop(dFdu ,dGdv ,dFdv ,dGdu);
        
        float dx = dop(F, dGdv, G, dFdv)/det; 
        float dy = dop(dFdu, G, dGdu, F)/det; 
        u -= dx;
        v -= dy;
        if (sop(dx/u, dx/u, dy/v, dy/v) <= rho2) {
            break;
        }
    }
    float F = eval(a1, a2, a3, a4, u, v);
    float G = eval(b1, b2, b3, b4, u, v);
    if(abs(F) <= 0.5E-6f && abs(G) <= 0.5E-6f)
        return new float[]{u, v};
    
    return new float[0];
}



  private float eval (final float a, final float b, final float c, final float d,
                      final float u, final float v) {
    
    // EvalÃºa la expresiÃ³n a + u * b + v * c + u * v * d
    // de la forma siguiente (u * d + c) * v + (u * b + a).

    final float t1 = fma(u, d, c);
    final float t2 = fma(u, b, a);
    return fma(t1, v, t2);

  }
}