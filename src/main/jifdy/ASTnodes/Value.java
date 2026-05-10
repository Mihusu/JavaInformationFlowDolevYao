package ASTnodes;

import java.io.Serializable;

abstract public class Value implements Serializable {
    public SecLabel label;

    public abstract Type getType();
}

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