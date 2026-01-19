package color.reflectance;
/**
 *
 * @author MAZ
 */
public abstract class FresnelianReflectance extends ReflectanceRGB {
  
  public FresnelianReflectance (final ReflectanceRGB rfc) {
    super(rfc);
  }
  
  public abstract boolean isDielectric ();
  public abstract boolean isConductive ();

}