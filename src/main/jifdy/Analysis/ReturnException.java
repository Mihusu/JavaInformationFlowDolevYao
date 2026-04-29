package Analysis;

import ASTnodes.Value;

public class ReturnException extends RuntimeException {
    public Value value;

    public ReturnException(Value value) {
        this.value = value;
    }
}
