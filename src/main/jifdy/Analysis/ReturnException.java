package Analysis;

import ASTNodes.Value;

/**
 * Exception used to signal a return from a method during interpretation.
 * Carries the return value back up the call stack.
 */
public class ReturnException extends RuntimeException {
    public Value value;

    public ReturnException(Value value) {
        this.value = value;
    }
}
