package color.reflectance;
/**
 *
 * @author MAZ
 */
public final class LambertianReflectance extends ReflectanceRGB  {
  
  static private final float INV_PI = (float) (1 / Math.PI);
           
  public LambertianReflectance (final float xr, final float xg, final float xb) {
    super(INV_PI * xr, INV_PI * xg, INV_PI * xb);
  }          
  
  public LambertianReflectance (final ReflectanceRGB diffuse) {
    super(diffuse.getR(), diffuse.getG(), diffuse.getB());
  }    
  
}
