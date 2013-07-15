package javax.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Denotes a parameter or field can be null.
 * <p/>
 * When decorating a method call parameter, this denotes the parameter can
 * legitimately be null and the method will gracefully deal with it. Typically used
 * on optional parameters.
 * <p/>
 * When decorating a method, this denotes the method might legitimately return null.
 * <p/>
 * This is a marker annotation and it has no specific attributes.
 */
@Retention(RetentionPolicy.CLASS)
public @interface Nullable {
}