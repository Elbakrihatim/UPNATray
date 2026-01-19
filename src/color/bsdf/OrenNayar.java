package color.bsdf;
/**
 *
 * @author MAZ
 *
 **/
import static java.lang.Math.fma;
import static java.lang.Math.max;
import org.jogamp.vecmath.Vector3f;
//
import color.BRDF;
import color.reflectance.ReflectanceRGB;
import engine.RadianceRGB;
//
public final class OrenNayar extends BRDF {

  private final float A;
  private final float B;

  public OrenNayar (final ReflectanceRGB diffuse,
                    final float _sigma) {
    super(diffuse);
    if ((Math.signum(_sigma) < 0) || (Math.signum(_sigma - 90.0f) > 0))
      throw new IllegalArgumentException("Valor sigma fuera de rango [0,90]");
    final float sigma = (float) Math.toRadians(_sigma);
    final float sigma2 = sigma * sigma;
    this.A = (1 - 0.50f * sigma2 / (sigma2 + (1.0f / 3)));
    this.B = (0.45f * sigma2 / (sigma2 + 0.09f));
  }

//    final float cosDiff = (float) Math.max(0, cosPhiI * cosPhiV + sinPhiI * sinPhiV);
//    final float frd = A + B * cosDiff * sinAlphaByTanBeta;
//
//    return diffuseFilter(nI * frd, inputRadiance);
//
//  }

//  @Override
//  RadianceRGB diffuseFilter (final RadianceRGB inputRadiance,
//                      final Vector3f in,
//                      final Vector3f out,
//                      final Vector3f n) {
//
//    final float nI = n.dot(in);
//    if ((inputRadiance == RadianceRGB.NORADIANCE) || (Math.signum(nI) <= 0))
//      return RadianceRGB.NORADIANCE;
//
//    final float vx = out.x;
//    final float vz = out.z;
//    final float v2 = vx * vx + vz * vz;
//
//    final float ix = in.x;
//    final float iz = in.z;
//    final float i2 = ix * ix + iz * iz;
//
////    final float d = (float) Math.sqrt(v2 * i2);
////    final float cosPhi = (vx * ix + vz * iz) / d;
//
//    final float cosPhi = vx * ix + vz * iz;
//
//    float frd = A;
//    if (Math.signum(cosPhi) > 0) {
//
//      System.out.println("*" + frd);
//
//      final float d2 = v2 * i2;
//
//      final float nV = n.dot(out);
//      final float cosBeta  = (float) Math.max(nI, nV);
//      final float cosAlpha = (float) Math.min(nI, nV);
//
//      final float sin2Alpha = 1.0f - cosAlpha * cosAlpha;
//      final float tan2Beta  = 1.0f / (cosBeta * cosBeta) - 1.0f;
//      final float sinAlphaByTanBeta = (float) Math.sqrt((sin2Alpha * tan2Beta) / d2);
//
//      frd += B * cosPhi * sinAlphaByTanBeta;
//
//      System.out.println(" " + frd);
//
//    } //else System.out.println(in + " " + out + " " + cosPhi);
//
//    return diffuse.reflectiveFilter(nI * frd, inputRadiance);
//
//  }

//  @Override
//  RadianceRGB diffuseFilter (final RadianceRGB inputRadiance,
//                             final Vector3f in,
//                             final Vector3f out,
//                             final Vector3f n,
//                             final float _nI) {
//
//    final float n2 = n.dot(n);
//    final float nu = n.dot(in);
//    final float nv = n.dot(out);
//    final float uv = in.dot(out);
//
//    final float cosPhi = Math.min(1.0f, uv - (nv * nu) / n2);
//
//    float frd = A;
//    if (Math.signum(cosPhi) > 0) {
//
//      final float nV = Math.min(1.0f, nv);
//      final float nI = Math.min(1.0f, nu);
//      final float cosBeta  = (float) Math.max(nI, nV);
//      final float cosAlpha = (float) Math.min(nI, nV);
//
//      if (Math.signum(cosBeta) != 0) {
//
//        final float sinAlpha2 = 1.0f - cosAlpha * cosAlpha;
//        final float sinBeta2  = 1.0f - cosBeta  * cosBeta;
//
//        final float fin  =  in.add(-nu / n2, n).lengthSquared();
//        final float fout = out.add(-nv / n2, n).lengthSquared();
//
//        // Se agrupan todos los factores que requieren extraer raíz cuadrada
//        final float sinAlphaBySinBeta = (float) Math.sqrt((sinAlpha2 * sinBeta2) / (fin * fout));
//
//        frd += B * cosPhi * sinAlphaBySinBeta / cosBeta;
//
//      }
//
//    }
//
//    return diffuse.reflectiveFilter(Math.min(1.0f, INV_PI * frd * nu), inputRadiance);
//
//  }

  @Override
  public RadianceRGB reflectiveFilter (final RadianceRGB inputRadiance,
                                       final Vector3f in,
                                       final Vector3f out,
                                       final Vector3f n,
                                       final float cosi) {
    return RadianceRGB.NORADIANCE;
  }

  @Override
  protected RadianceRGB diffuseFilter (final RadianceRGB inputRadiance,
                                       final Vector3f wi,
                                       final Vector3f wo,
                                       final Vector3f n,
                                       final float cosi) {
    return RadianceRGB.NORADIANCE;
  } 

}