package model3d.kdtree;
/**
 *
 * @author MAZ
 */
import org.jogamp.vecmath.Point3f;
import raytracer.Hit;
import raytracer.Ray;
//
final class EmptyLeaf extends Node {

  static final EmptyLeaf EMPTY_LEAF = new EmptyLeaf();

  private EmptyLeaf () {}

  @Override
  Hit intersect (final boolean fromInside, final Ray ray,
                 final float tin, final float tout) {
    return Hit.NOHIT;
  }

  @Override
  boolean intersectAny (final Ray ray, final float tin, final float tout) {
    return false;
  }

  @Override
  boolean isInside (final Point3f P) { return false; }

  @Override
  boolean isOutside (final Point3f P) { return true; }
  
}
