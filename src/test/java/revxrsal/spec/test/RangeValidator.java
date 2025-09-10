package revxrsal.spec.test;

import java.util.function.Consumer;
import revxrsal.spec.SpecProperty;
import revxrsal.spec.validation.PropertyValidator;
import revxrsal.spec.validation.SpecValidationException;

public class RangeValidator implements PropertyValidator {

    @Override
    public Consumer<Object> createValidator(SpecProperty property) {
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
                throw new SpecValidationException(
                    String.format("Value is too small: '%f' against '%f' required!", n,
                        range.min()));
            } else if (range.max() < n) {
                throw new SpecValidationException(
                    String.format("Value is too big: '%f' against '%f' required!", n, range.max()));
            }
        };
    }
}
