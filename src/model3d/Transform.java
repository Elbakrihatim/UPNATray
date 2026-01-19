package model3d;

/**
 *
 * @author MAZ
 */
import static java.lang.Math.signum;
import org.jogamp.vecmath.Matrix4f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Tuple3f;
import org.jogamp.vecmath.Vector3f;
//
import model3d.boundingvolume.AABB;
import raytracer.Hit;
import raytracer.Ray;
//

public class Transform {

    static public final Transform IDENTITY_TRANSFORM = new IdentityTransform();

    public final Matrix4f worldToModel; // Matriz de transformación inversa
    public final Matrix4f modelToWorld; // Matriz de transformación directa
    public final Matrix4f N;            // Matriz para ajuste de normales
    public final boolean scaled;        // Indica si incluye un cambio de escala.

    static public Matrix4f getAimingMatrix(final Vector3f i,
            final Vector3f j,
            final Vector3f k) {

        final Matrix4f A = new Matrix4f();

        // Formación de la matriz A en base a los vectores i, j y k dados.
        A.setRow(0, i.x, i.y, i.z, 0);
        A.setRow(1, j.x, j.y, j.z, 0);
        A.setRow(2, k.x, k.y, k.z, 0);
        A.setRow(3, 0, 0, 0, 1);

        return A;

    }

    public static Matrix4f getRotationMatrix(final Vector3f axis, final float theta) {
        final Matrix4f R = new Matrix4f();

        if (Math.signum(theta) != 0) {
            // Normalizar el vector que define el eje de rotación.
            Vector3f normalizedAxis = new Vector3f(axis);
            normalizedAxis.normalize();

            float cosTheta = (float) Math.cos(theta);
            float sinTheta = (float) Math.sin(theta);
            float oneMinusCosTheta = 1.0f - cosTheta;

            // Extraer componentes del eje normalizado
            float x = normalizedAxis.x;
            float y = normalizedAxis.y;
            float z = normalizedAxis.z;

            float r1, r2, r3, r4;

            // Construir la matriz de rotación usando la fórmula de Rodrigues       
            R.setRow(0,
                    cosTheta + x * x * oneMinusCosTheta,
                    x * y * oneMinusCosTheta - z * sinTheta,
                    x * z * oneMinusCosTheta + y * sinTheta, 0);

            R.setRow(1,
                    y * x * oneMinusCosTheta + z * sinTheta,
                    cosTheta + y * y * oneMinusCosTheta,
                    y * z * oneMinusCosTheta - x * sinTheta, 0);

            R.setRow(2,
                    z * x * oneMinusCosTheta - y * sinTheta,
                    z * y * oneMinusCosTheta + x * sinTheta,
                    cosTheta + z * z * oneMinusCosTheta, 0);

            R.setRow(3, 0, 0, 0, 1);

        } else {
            R.setIdentity();
        }

        return R;
    }

    static public Matrix4f getTranslationMatrix(final Vector3f d) {
        final Matrix4f T = new Matrix4f();
        
        // Formación de la matriz de traslación
        T.setRow(0, 1, 0, 0, d.x);
        T.setRow(1, 0, 1, 0, d.y);
        T.setRow(2, 0, 0, 1, d.z);
        T.setRow(3, 0, 0, 0, 1);

        return T;
    }

    public static Matrix4f getScaleMatrix(final Tuple3f s) {
        final Matrix4f S = new Matrix4f();

        // Formación de la matriz de cambio de escala
        S.setRow(0, s.x, 0, 0, 0);
        S.setRow(1, 0, s.y, 0, 0);
        S.setRow(2, 0, 0, s.z, 0);
        S.setRow(3, 0, 0, 0, 1);
        return S;
    }


    public Transform(final Matrix4f scaleMatrix,final Matrix4f aimingMatrix,final Matrix4f rotationMatrix,final Matrix4f translationMatrix,
            final boolean scaled) {
    this.modelToWorld = new Matrix4f(translationMatrix); 
    this.modelToWorld.mul(rotationMatrix); 
    this.modelToWorld.mul(aimingMatrix);              
    this.modelToWorld.mul(scaleMatrix);         

    Matrix4f invScaleMatrix = new Matrix4f(scaleMatrix);
    invScaleMatrix.m00 = 1.0f / scaleMatrix.m00;
    invScaleMatrix.m11 = 1.0f / scaleMatrix.m11;
    invScaleMatrix.m22 = 1.0f / scaleMatrix.m22;

    Matrix4f invRotationMatrix = new Matrix4f(rotationMatrix);
    invRotationMatrix.transpose(); // La inversa de una matriz de rotación es su transpuesta
    
    Matrix4f invAimingMatrix = new Matrix4f(aimingMatrix);
    invAimingMatrix.transpose();
    
    Matrix4f invTranslationMatrix = new Matrix4f(translationMatrix);
    //invTranslationMatrix.setIdentity();
    
    
    invTranslationMatrix.m03 = -translationMatrix.m03;
    invTranslationMatrix.m13 = -translationMatrix.m13;
    invTranslationMatrix.m23 = -translationMatrix.m23;
    // Construir worldToModel en el orden inverso: 
    this.worldToModel = new Matrix4f(invScaleMatrix);
    this.worldToModel.mul(invAimingMatrix);
    this.worldToModel.mul(invRotationMatrix);         
    this.worldToModel.mul(invTranslationMatrix);     

    // Continuar con la matriz N
    this.N = new Matrix4f(rotationMatrix);
    N.mul(aimingMatrix);
    N.mul(invScaleMatrix);
    this.scaled = scaled;

    }

    public Transform(final Transform transform) {
        this(new Matrix4f(transform.modelToWorld),
                new Matrix4f(transform.worldToModel),
                new Matrix4f(transform.N),
                transform.scaled);
    }

    public Transform(final Matrix4f modelToWorld,
            final Matrix4f worldToModel,
            final Matrix4f N) {
        this(modelToWorld, worldToModel, N, true);
    }

    public Transform(final Matrix4f modelToWorld,
            final Matrix4f worldToModel,
            final Matrix4f N,
            final boolean scaled) {
        this.worldToModel = new Matrix4f(worldToModel);
        this.modelToWorld = new Matrix4f(modelToWorld);
        this.N = new Matrix4f(N);
        this.scaled = scaled;
    }
    

    public void modelToWorld(final Point3f P) {
        modelToWorld.transform(P);
    }

    public void worldToModel(final Point3f P) {
        worldToModel.transform(P);
    }

    public void modelToWorld(final Vector3f w) {
        modelToWorld.transform(w);
    }

    public void worldToModel(final Vector3f w) {
        worldToModel.transform(w);
    }

    public Ray worldToModel(final Ray ray) {
        final Point3f R = new Point3f(ray.getStartingPoint());
        final Vector3f v = new Vector3f(ray.getDirection());
        worldToModel.transform(R);
        worldToModel.transform(v);
        return new Ray(R, v);
    }

    public Hit modelToWorld(final Hit hit, final Point3f R) {

        // Se transforma el punto contenido en el hit: tras la actuación
        // de la matriz modelToWorld, el punto queda en coordenadas de escena.
        final Point3f P = hit.getPoint();
        modelToWorld.transform(P);

        // Se transforma el vector normal contenido en el hit: tras la actuación
        // de la matriz modelToWorld, el vector queda en coordenadas de escena.
        final Vector3f n = hit.getNormal();
        N.transform(n);

        // Si la transformación incluye un cambio de escala, se cómputa el valor
        // del parámetro alfa como la distancia entre el punto R y el punto P
        // descritos en coordenadas de escena.
        if (scaled) {
            final float a = P.distance(R);
            hit.setAlpha(a);
        }

        return hit;
    }

    public AABB modelToWorld(final AABB boundingBox) {
        return boundingBox.transformedBy(modelToWorld);
    }

    public Model3D transform(final Model3D model) {
        return null;
    }

    public Transform compose(final Transform transform) {
        if (this != IDENTITY_TRANSFORM) {
            worldToModel.mul(transform.worldToModel);
            final Matrix4f _modelToWorld = new Matrix4f(transform.modelToWorld);
            _modelToWorld.mul(modelToWorld);
            modelToWorld.set(_modelToWorld);
            final Matrix4f _N = new Matrix4f(transform.N);
            _N.mul(N);
            N.set(_N);
            return this;
        } else {
            return (transform != IDENTITY_TRANSFORM) ? new Transform(transform) : this;
        }
    }

    static private final class IdentityTransform extends Transform {

        static private final Matrix4f I = getIdentityMatrix();

        static private Matrix4f getIdentityMatrix() {
            final Matrix4f _I = new Matrix4f();
            _I.setIdentity();
            return _I;
        }

        private IdentityTransform() {
            super(I, I, I);
        }

        @Override
        public void modelToWorld(final Point3f P) {
        }

        @Override
        public void worldToModel(final Point3f P) {
        }

        @Override
        public void modelToWorld(final Vector3f w) {
        }

        @Override
        public void worldToModel(final Vector3f w) {
        }

        @Override
        public Ray worldToModel(final Ray ray) {
            return ray;
        }

        @Override
        public Hit modelToWorld(final Hit hit, final Point3f R) {
            return hit;
        }

    }

}
