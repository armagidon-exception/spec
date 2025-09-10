package revxrsal.spec.test;

import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import revxrsal.spec.PostProcessor;
import revxrsal.spec.SpecProperty;

public class RangeHook implements PostProcessor {

    @Override
    public @Nullable Consumer<Object> createReadHook(SpecProperty property) {
        return makeHook(property);
    }

    @Override
    public @Nullable Consumer<Object> createWriteHook(SpecProperty property) {
        return makeHook(property);
    }

    private Consumer<Object> makeHook(SpecProperty property) {
        return value -> {
            if (!(value instanceof Number)) {
                return;
            }
            if (!property.getter().isAnnotationPresent(Range.class)) {
                return;
            }
            double n = ((Number) value).doubleValue();
            Range range = property.getter().getAnnotation(Range.class);
            if (range.min() > n) {
                throw new RuntimeException(
                    String.format("Value is too small: '%f' against '%f' required!", n,
                        range.min()));
            } else if (range.max() < n) {
                throw new RuntimeException(
                    String.format("Value is too big: '%f' against '%f' required!", n, range.max()));
            }
        };
    }
}
