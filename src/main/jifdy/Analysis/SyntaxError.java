package Analysis;

/**
 * Exception thrown when a syntax error is encountered during parsing.
 */
public class SyntaxError extends RuntimeException {

    public SyntaxError(String message) {
        super(message);
    }
}
