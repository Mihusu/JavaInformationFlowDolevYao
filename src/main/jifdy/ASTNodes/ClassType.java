package ASTNodes;

import java.util.Objects;

/**
 * Type descriptor for a source-language class.
 */
public record ClassType(String name) implements Operators {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClassType other && Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
