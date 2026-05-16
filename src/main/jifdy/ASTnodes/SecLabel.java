package ASTnodes;

/**
 * Two-point security lattice used for information-flow checks.
 */
public class SecLabel {
    public static final SecLabel HIGH = new SecLabel("HIGH");
    public static final SecLabel LOW = new SecLabel("LOW");

    private final String name;

    private SecLabel(String name) {
        this.name = name;
    }

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
}
