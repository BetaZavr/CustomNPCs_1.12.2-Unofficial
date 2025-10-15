package noppes.npcs.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to automatically create API types
 */
@Retention(RetentionPolicy.RUNTIME)
@IgnoreForAPI
public @interface ParamName {
    String value();
}
