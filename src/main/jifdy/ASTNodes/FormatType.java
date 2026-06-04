package ASTNodes;

import java.util.List;
import java.util.Objects;

public class FormatType implements Operators {
    public String name;
    public List<Param> fields;

    public FormatType(String name, List<Param> fields) {
        this.name = name;
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
