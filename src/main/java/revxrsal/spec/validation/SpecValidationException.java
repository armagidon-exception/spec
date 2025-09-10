package revxrsal.spec.validation;

/**
 * Thrown if property does not pass validator successfully.
 */
public class SpecValidationException extends RuntimeException {

    public SpecValidationException(String message) {
        super(message);
    }
}
