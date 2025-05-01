package revxrsal.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that allows to compute certain values and cache their result.
 * <p>
 * This is very useful for heavy, repetitive computations that depend
 * on the configuration values.
 * <p>
 * Note: {@link Memoize @Memoize} does not (yet) consider arguments
 * when caching values. Therefore, it is best to just use it to compute the
 * parts that depend on the configuration values
 * <p>
 * Reloading, resetting, or calling a setter will re-compute
 * all memoized values.
 * <p>
 * Example:
 * <pre>{@code @ConfigSpec
 * public interface SearchArea {
 *
 *     default double radius() {
 *         return 5;
 *     }
 *
 *     void setRadius(double radius);
 *
 *     @Memoize
 *     default double radiusCubed() {
 *         System.out.println("Computing r^3");
 *         return radius() * radius() * radius();
 *     }
 *
 *     @Memoize
 *     default double radiusSquared() {
 *         System.out.println("Computing r^2");
 *         return radius() * radius();
 *     }
 * }}</pre>
 *
 * <pre>{@code
 * SearchArea area = Specs.createDefault(SearchArea.class);
 * System.out.println(area.radiusCubed());
 * System.out.println(area.radiusCubed());
 * area.setRadius(10);
 * System.out.println(area.radiusCubed());
 * System.out.println(area.radiusCubed());
 * }</pre>
 * Will print:
 * <pre>
 * Computing r^3
 * 125.0
 * 125.0
 * Computing r^3
 * 1000.0
 * 1000.0
 * </pre>
 */
@HandledByProxy
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Memoize {
}
