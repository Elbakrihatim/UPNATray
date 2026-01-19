package raytracer;
//

import static java.lang.Math.fma;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
import org.jogamp.vecmath.TexCoord2f;
//
import model3d.Model3D;
import color.VModel;
//

public final class Hit {

    static public final Hit NOHIT = new Hit();

    // ATRIBUTOS  
    private float alpha;
    private Vector3f normal;
    private Point3f P;        // Punto de intersección
    private TexCoord2f uv;
    private Model3D model3d;
    private VModel vmodel;

    // CONSTRUCTORES
    private Hit() {
        this.alpha = Float.POSITIVE_INFINITY;
    }

    private Hit(final float alpha,
            final Point3f P,
            final Vector3f n,
            final TexCoord2f uv,
            final Model3D model3d,
            final VModel vmodel) {
        this.alpha = alpha;
        this.P = P;
        this.normal = n;
        this.uv = uv;
        this.model3d = model3d;
        this.vmodel = vmodel;
    }

    public Hit(final float alpha,
            final Point3f P,
            final Vector3f n,
            final TexCoord2f uv) {
        this(alpha, P, n, uv, null, null);
    }

    public Hit(final float alpha,
            final Point3f P,
            final Vector3f n) {
        this(alpha, P, n, null, null, null);
    }

    public Hit(final Hit h) {
        this(h.getAlpha(),
                new Point3f(h.getPoint()),
                new Vector3f(h.getNormal()),
                new TexCoord2f(h.getTextureCoordenates()),
                h.model3d,
                h.getVisualModel());
    }

    public Vector3f getNormal() {
        return normal;
    }

    public void setNormal(final Vector3f normal) {
        this.normal = normal;
    }

    public Point3f getPoint() {
        return P;
    }

    public Point3f getShiftedPoint() {
        final float shift = 0.001f;
        return new Point3f(fma(shift, normal.x, P.x),
                fma(shift, normal.y, P.y),
                fma(shift, normal.z, P.z));
    }

    public void setPoint(final Point3f p) {
        this.P = p;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(final float t) {
        this.alpha = t;
    }

    public TexCoord2f getTextureCoordenates() {
        return uv;
    }

    public Model3D getModel3D() {
        return model3d;
    }

    public void setModel3D(final Model3D model3d) {
        this.model3d = model3d;
    }

    public VModel getVisualModel() {
        return vmodel;
    }

    public void setVisualModel(final VModel vmodel) {
        this.vmodel = vmodel;
    }

    public boolean isCloserThan(final Hit h) {
        return getAlpha() < h.getAlpha();
    }

    public boolean hits() {
        return (this.alpha != Float.POSITIVE_INFINITY);
    }

    @Override
    public String toString() {
        return P + " ";
    }

}
