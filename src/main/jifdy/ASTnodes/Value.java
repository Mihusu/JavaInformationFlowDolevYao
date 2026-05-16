package ASTnodes;

import java.io.Serializable;

/**
 * Represents a value in the language's runtime.
 * All values have an associated security label.
 */
abstract public class Value implements Serializable {
    /**
     * The security label of this value.
     */
    public SecLabel label;

    /**
     * Retrieves the type of the value.
     * @return The type.
     */
    public abstract Type getType();
}

/**
 * Runtime integer value.
 */
class IntValue extends Value implements Serializable {
    int value;

    public IntValue(int i) {
        this.value = i;
    }

    @Override
    public Type getType() {
        return Type.INT;
    }
}

/**
 * Runtime boolean value.
 */
class BoolValue extends Value implements Serializable {
    boolean value;

    public BoolValue(boolean equals) {
        this.value = equals;
    }

    @Override
    public Type getType() {
        return Type.BOOL;
    }
}

/**
 * Runtime string value.
 */
class StringValue extends Value implements Serializable {
    String value;

    public StringValue(String key) {
        this.value = key;
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }
}
