package model3d.shape;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import java.util.Map;
import java.util.Set;
import org.jogamp.vecmath.Point3f;
//
import static raytracer.Hit.NOHIT;
import model3d.boundingvolume.AABB;
import model3d.facet.Facet;
import model3d.facet.Vertex3D;
import model3d.kdtree.KdTree;
import raytracer.Hit;
import raytracer.Ray;
//
public class PolygonalMesh implements Shape3D {
  
  private KdTree kdTree;  
  private final AABB boundingBox;
  private final Set<Facet> facets;
  private final Map<Vertex3D, Set<Facet>> vertexToFacetMap;
      
  public PolygonalMesh (final Set<Facet> facets,
                        final Map<Vertex3D, Set<Facet>> vertexToFacetMap) {

    this.facets = facets;
    this.vertexToFacetMap = vertexToFacetMap;
    this.boundingBox = new AABB(vertexToFacetMap.keySet());       
    this.kdTree = null;

  }
  
  public void set (final KdTree kdTree) {
    kdTree.set(this);
    this.kdTree = kdTree;  
  }

  @Override
  public Hit intersectFromOutside (final Ray ray) {
    final float[] hits = boundingBox.intersectComplete(ray);
    if (hits.length == 2) {
      final float tmin = hits[0];
      final float tmax = hits[1];
      final Hit hit = kdTree.intersectFromOutside(ray, tmin, tmax);
      return hit;
    }
    return NOHIT;
  }
  
  @Override
  public Hit intersectFromInside (final Ray ray) {
    final float[] hits = boundingBox.intersectComplete(ray);
    return kdTree.intersectFromInside(ray, 0, hits[1]);
  }

  @Override
  public boolean isInside (final Point3f P) {
    return boundingBox.isInside(P) ? kdTree.isInside(P) : false;
  }

  @Override
  public boolean isOutside (final Point3f P) {
    return boundingBox.isOutside(P) ? true : kdTree.isOutside(P);
  }

  @Override
  public Hit intersect (boolean fromOutside, Ray ray) {
    if (fromOutside)
      return intersectFromOutside(ray);
    else
      return intersectFromInside(ray);
  }

  @Override
  public AABB getBoundingBox () {
    return boundingBox;
  }

  @Override
  public Hit intersect (boolean fromOutside, final Ray ray,
                        final float tmin, final float tmax) {
    final float[] hits = boundingBox.intersectComplete(ray);
    if (hits.length == 2) {
      final float _tmin = max(hits[0], tmin);
      final float _tmax = min(hits[1], tmax);
      if (signum(_tmax - _tmin) < 0)
        return NOHIT;
      final Hit hit = fromOutside ?
              kdTree.intersectFromOutside(ray, _tmin, _tmax) :
              kdTree.intersectFromInside(ray, _tmin, _tmax);
      return hit;
    }
    return NOHIT;
  }

  @Override
  public boolean intersectAny (final Ray ray, final float tmin, final float tmax) {
    final float[] hits = boundingBox.intersectComplete(ray);
    if (hits.length == 2) {
      final float _tmin = max(hits[0], tmin);
      final float _tmax = min(hits[1], tmax);
      return (signum(_tmin - _tmax) <= 0) ? kdTree.intersectAny(ray, _tmin, _tmax) : false;
    }
    return false;
  }
  
  public Set<Facet> getFacets () { return facets; }
  public Map<Vertex3D, Set<Facet>> getMap () { return vertexToFacetMap; }
  
}