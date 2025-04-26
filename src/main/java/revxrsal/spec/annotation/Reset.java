package revxrsal.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a function as being the reset function for a {@link ConfigSpec}.
 * <p>
 * This will restore the config spec to its default state. All values will
 * be set to their default values.
 * <p>
 * Example:
 * <pre>{@code @ConfigSpec
 * public interface GameSettings {
 *
 *     @Comment("The cooldown message. Use %cooldown% as a placeholder.")
 *     default String cooldownMessage() {
 *         return "Starting in %cooldown%s";
 *     }
 *
 *     @Reset
 *     void reset();
 * }}</pre>
 */
@HandledByProxy
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Reset {
}
