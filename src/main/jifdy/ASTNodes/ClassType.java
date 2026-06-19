package ASTNodes;

import java.util.Objects;

/**
 * Type descriptor for a source-language class.
 */
public class ClassType extends Types {
    public final String name;

    public ClassType(String name) {
        super(Type.OBJECT);
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClassType other && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
