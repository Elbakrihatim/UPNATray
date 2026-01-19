package model3d.kdtree;
/**
 *
 * @author MAZ
 */
import static java.lang.Math.signum;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jogamp.vecmath.Point3f;
//
import static raytracer.Hit.NOHIT;
import model3d.boundingvolume.AABB;
import model3d.boundingvolume.BoundingVolume;
import model3d.facet.Facet;
import model3d.facet.Vertex3D;
import raytracer.Hit;
import raytracer.Ray;
//
final class Leaf extends Node {

  private final Facet[] facets;
  private final BoundingVolume boundingBox;
  
  Leaf (final Collection<Facet> facets) {
    final Set<Vertex3D> vertices = new HashSet<>();
    for (final Facet f: facets)
      vertices.addAll(f.getVertices());
    this.boundingBox = new AABB(vertices);
    this.facets = facets.toArray(Facet[]::new);    
  }  
  
  Leaf (final Set<Vertex3D> vertices,
        final Map<Vertex3D, Set<Facet>> vertexToFacetMap) {
    final Set<Facet> _facets = new HashSet<>();
    for (final Vertex3D V: vertices)
      _facets.addAll(vertexToFacetMap.get(V));
    final Set<Vertex3D> _vertices = new HashSet<>();
    for (final Facet f: _facets)
      _vertices.addAll(f.getVertices());    
    this.boundingBox = new AABB(_vertices);
    this.facets = _facets.toArray(Facet[]::new);    
  }  

    @Override
    Hit intersect(final boolean fromOutside, final Ray ray,
                  final float tmin, final float tmax) {

        // Verificar la intersección con el bounding box primero
        if(boundingBox.intersect(ray,tmin,tmax))
        {
            Hit closesHit = NOHIT;
            float _tmax = tmax;
            for (final Facet facet: facets)
            {
                final Hit hit = facet.intersect(fromOutside, ray, tmin, _tmax);
                if(hit.isCloserThan(closesHit))
                {
                    closesHit = hit;
                    _tmax = hit.getAlpha();
                }
            }
            return closesHit;
        }
        return NOHIT;
    }
  
  @Override
  boolean intersectAny (final Ray ray, final float tmin, final float tmax) {

    if (signum(tmin - tmax) > 0) {
      throw new IllegalArgumentException(tmin + "(tmin) > " + tmax + " (tmax)");
    }

    // Hay que determinar si el rayo intersecta con la faceta y devolver
    // true o false.
    
    
    return boundingBox.intersect(ray,tmin,tmax);
    
  }
  
  @Override
  boolean isInside (final Point3f P) { return boundingBox.isInside(P); }

  @Override
  boolean isOutside (final Point3f P) { return boundingBox.isOutside(P); }  

}
