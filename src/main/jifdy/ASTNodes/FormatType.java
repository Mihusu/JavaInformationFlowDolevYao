package ASTNodes;

import java.util.List;
import java.util.Objects;

/**
 * Type descriptor for a declared Dolev-Yao message format.
 *
 * <p>
 * The format type stores the format name and its ordered field declarations.
 * Those fields are used when constructing format values and when inferring
 * bindings in receive patterns.
 * </p>
 */
public class FormatType extends Types {
    public String name;
    public List<Param> fields;

    public FormatType(String name, List<Param> fields) {
        super(Type.FORMAT);
        this.name = name;
        this.formatName = name;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FormatType other)) {
            return false;
        }

        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
