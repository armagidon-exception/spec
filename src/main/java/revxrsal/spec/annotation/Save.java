package revxrsal.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a function as being the reload function for a {@link ConfigSpec}.
 * <p>
 * Note that only top-level values can be saved!
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
 *     @Save
 *     void save();
 * }}</pre>
 */
@HandledByProxy
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Save {
}
