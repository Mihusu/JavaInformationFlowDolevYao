package ASTNodes;

import java.util.HashMap;
import java.util.Map;

/**
 * Runtime instance for a source-language class.
 */
public class ObjectValue extends Value {
    public final String className;
    public final Map<String, Value> fields = new HashMap<>();
    public final Map<String, SecLabel> fieldLabels = new HashMap<>();

    public ObjectValue(String className) {
        this.className = className;
        this.label = SecLabel.LOW;
    }

    @Override
    public Type getType() {
        return Type.OBJECT;
    }

    @Override
    public String toString() {
        return className + "{...}";
    }
}
