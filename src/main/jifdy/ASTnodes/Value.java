package ASTnodes;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        // Needed by ExprFormat.match: computed pattern fields compare Value objects.
        return obj instanceof IntValue other && value == other.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
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

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        // Needed by ExprFormat.match: computed pattern fields compare Value objects.
        return obj instanceof BoolValue other && value == other.value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }
}

/**
 * Runtime string value.
 */
class StringValue extends Value implements Serializable {
    String value;

    public StringValue(String keyName) {
        this.value = keyName;
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        // Needed by ExprFormat.match: computed pattern fields compare Value objects.
        return obj instanceof StringValue other && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
