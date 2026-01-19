package view;

/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Matrix4f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
//
import raytracer.RayGenerator;
//

public class Camera {

    // Punto de ubicación de la cámara
    private final Point3f V;
    // Vector de vista (lookAt)
    private final Vector3f view;
    // Inversa de la matriz de transformación de vista
    private final Matrix4f camera2scene;
    // Proyección a aplicar
    private Projection optics;

    // CONSTRUCTOR
    public Camera(final Point3f V, final Point3f C, final Vector3f up) {

        optics = null;
        this.V = new Point3f(V);
        this.view = new Vector3f();
        this.view.sub(C, V);
        this.view.normalize();

        // Formación de la matriz de la inversa de la transformación de vista.
        float s = up.dot(view);
        float t = (float) (1 / Math.sqrt(1 - Math.pow(s, 2)));
        
        camera2scene = new Matrix4f();
        
        camera2scene.setRow(0, t * (up.getZ() * view.getY() - up.getY() * view.getZ()),
                t * (up.getX() - (s * view.getX())), -view.getX(), this.V.getX());

        camera2scene.setRow(1, t * (up.getX() * view.getZ()) - (up.getZ() * view.getX()),
                t * (up.getY() - (s * view.getY())), -view.getY(), this.V.getY());
        
        camera2scene.setRow(2, t * (up.getY() * view.getX() - up.getX() * view.getY()),
                t * (up.getZ() - (s * view.getZ())), -view.getZ(), this.V.getZ());

        camera2scene.setRow(3, 0, 0, 0, 1);

    }

    public Camera(final Camera c) {
        this.V = new Point3f(c.V);
        this.view = new Vector3f(c.view);
        this.camera2scene = new Matrix4f(c.camera2scene);
        this.optics = c.optics;
    }

    public final void toSceneCoordenates(final Vector3f v) {
        camera2scene.transform(v);
    }

    public final void toSceneCoordenates(final Point3f P) {
        camera2scene.transform(P);
    }

    public final Vector3f getLook() {
        return this.view;
    }

    public final Point3f getPosition() {
        return this.V;
    }

    public final void setProjection(final Projection p) {
        this.optics = p;
    }

    public final Projection getProjection() {
        return this.optics;
    }

    public final RayGenerator getRayGenerator(final int W, final int H) {
        return this.optics.getRayGenerator(this, W, H);
    }

}
