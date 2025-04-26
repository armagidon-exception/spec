package revxrsal.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the index of the spec field according to other fields. This helps
 * ensure values come out in a specific order, as neither Gson nor Java guarantee
 * the field order.
 * <p>
 * Lower values come first.
 * <p>
 * Example:
 * <pre>{@code @ConfigSpec
 * public interface GameSettings {
 *
 *     @Order(0)
 *     default int cooldown() {
 *         return 20;
 *     }
 *
 *     @Order(1)
 *     String cooldownMessage();
 * }}</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {

    /**
     * The index value
     *
     * @return The index value
     */
    int value();

}
