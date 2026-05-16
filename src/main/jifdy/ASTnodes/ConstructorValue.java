package ASTnodes;

import java.util.List;

/**
 * Runtime value that stores a constructor name together with its payload values.
 */
public class ConstructorValue extends Value {
    String name;
    List<Value> values;

    public ConstructorValue(String name, List<Value> values) {
        this.name = name;
        this.values = values;
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }
}
