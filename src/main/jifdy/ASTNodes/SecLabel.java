package ASTNodes;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;

/**
 * Two-point security lattice used for information-flow checks.
 */
public class SecLabel implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final SecLabel HIGH = new SecLabel("HIGH");
    public static final SecLabel LOW = new SecLabel("LOW");

    private final String name;

    private SecLabel(String name) {
        this.name = name;
    }

    // Checks if two labels are equal to see if it's HIGH or LOW
    // Checks the highest label of the two and if it's HIGH for both secLabels, return HIGH'
    public static SecLabel join(SecLabel a, SecLabel b) {
        if (a == HIGH || b == HIGH) {
            return HIGH;
        }
        return LOW;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Preserve canonical HIGH/LOW instances across serialization so existing
     * identity checks remain valid after decryption.
     */
    @Serial
    private Object readResolve() throws ObjectStreamException {
        if ("HIGH".equals(name)) {
            return HIGH;
        }
        if ("LOW".equals(name)) {
            return LOW;
        }
        throw new IllegalStateException("Unknown security label: " + name);
    }
}
