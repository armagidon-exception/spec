package revxrsal.spec.validation;

import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import revxrsal.spec.PostProcessor;
import revxrsal.spec.SpecProperty;

/**
 * Interface for implementing a PropertyValidator
 * See {@link SpecValidationException}
 */
public interface PropertyValidator extends PostProcessor {

    @Override
    default @Nullable Consumer<Object> createReadHook(SpecProperty property) {
        return createValidator(property);
    }

    @Override
    default @Nullable Consumer<Object> createWriteHook(SpecProperty property) {
        return createValidator(property);
    }

    Consumer<Object> createValidator(SpecProperty property);
}
