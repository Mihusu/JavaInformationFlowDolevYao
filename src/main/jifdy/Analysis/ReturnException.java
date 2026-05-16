package Analysis;

import ASTnodes.Value;

/**
 * Exception used to signal a return from a function during interpretation.
 * Carries the return value back up the call stack.
 */
public class ReturnException extends RuntimeException {
    public Value value;

    public ReturnException(Value value) {
        this.value = value;
    }
}
