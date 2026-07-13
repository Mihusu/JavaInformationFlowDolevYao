package ASTNodes;

import java.io.Serializable;

/**
 * Two-point security lattice used for information-flow checks.
 */
public class SecLabel implements Serializable {
    public static final SecLabel HIGH = new SecLabel("HIGH");
    public static final SecLabel LOW = new SecLabel("LOW");

    private final String name;

    private SecLabel(String name) {
        this.name = name;
    }

    /**
     * Computes the least upper bound of two labels.
     *
     * @param a First label.
     * @param b Second label.
     * @return {@link #HIGH} if either label is high; otherwise {@link #LOW}.
     */
    public static SecLabel supremum(SecLabel a, SecLabel b) {
        if (a == HIGH || b == HIGH) {
            return HIGH;
        }
        return LOW;
    }

    /**
     * Computes the greatest lower bound of two labels which is the infimum of.
     *
     * <p>
     * This is used for the DYIF assignment side condition
     * {@code currentProdcedure <= infimum(Gamma(x), underline(Delta(x)))}.
     * </p>
     *
     * @param a First label.
     * @param b Second label.
     * @return {@link #LOW} if either label is low; otherwise {@link #HIGH}.
     */
    public static SecLabel infimum(SecLabel a, SecLabel b) {
        if (a == LOW || b == LOW) {
            return LOW;
        }
        return HIGH;
    }

    @Override
    public String toString() {
        return name;
    }
}
