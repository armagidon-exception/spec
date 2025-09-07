package revxrsal.spec;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.spec.Util.JsonPathUtils;

/**
 * This exception is thrown when an exception occurs inside {@link com.google.gson.TypeAdapter}
 * It allows to record current path of the {@link com.google.gson.stream.JsonReader} to give to
 * the user.
 */
public class SpecSerializationException extends IOException {

  private final @Unmodifiable List<Object> path;

  /**
   * @param path list of nodes that form json path e.g. key names in objects and 1-base indices inside arrays
   * @param cause Causing exception
   */
  public SpecSerializationException(List<Object> path, Throwable cause) {
    super(buildMessage(path, cause.getMessage()), cause);
    this.path = Collections.unmodifiableList(path);
  }

  /**
   * @param path list of nodes that form json path e.g. key names in objects and 1-base indices inside arrays
   * @param message Message to the user
   */
  public SpecSerializationException(List<Object> path, String message) {
    super(buildMessage(path, message));
    this.path = Collections.unmodifiableList(path);
  }

  public SpecSerializationException(JsonReader reader, String message) {
    this(JsonPathUtils.getJsonPath(reader), message);
  }

  public SpecSerializationException(JsonReader reader, Throwable cause) {
    this(JsonPathUtils.getJsonPath(reader), cause);
  }

  private static String buildMessage(List<Object> path, String message) {
    return String.format("%s: %s", path, message);
  }

  public @Unmodifiable List<Object> getPath() {
    return path;
  }

}
