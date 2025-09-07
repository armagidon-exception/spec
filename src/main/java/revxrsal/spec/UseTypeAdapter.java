package revxrsal.spec;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets either a {@link TypeAdapter} or {@link TypeAdapterFactory}, or one or both of
 * {@link JsonDeserializer} or {@link JsonSerializer} for property that
 * this annotation is applied to. This is annotation is different from {@link com.google.gson.annotations.JsonAdapter}
 * as {@link com.google.gson.annotations.JsonAdapter} is applied on a type, but this annotation is applied on a spec property.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseTypeAdapter {

  /**
   * Either a {@link TypeAdapter} or {@link TypeAdapterFactory}, or one or both of
   * {@link JsonDeserializer} or {@link JsonSerializer}.
   */
  Class<?> value();

}
