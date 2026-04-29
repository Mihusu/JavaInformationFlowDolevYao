package ASTnodes;

abstract public class Value {
    public SecLabel label;

    public abstract Type getType();
}

class IntValue extends Value {
    int value;

    public IntValue(int i) {
        this.value = i;
    }

    @Override
    public Type getType() {
        return Type.INT;
    }
}

class BoolValue extends Value {
    boolean value;

    public BoolValue(boolean equals) {
        this.value = equals;
    }

    @Override
    public Type getType() {
        return Type.BOOL;
    }
}

class StringValue extends Value {
    String value;

    public StringValue(String key) {
        this.value = key;
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }
}