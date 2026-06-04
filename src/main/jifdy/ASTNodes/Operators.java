package ASTNodes;

/**
 * Marker interface for values that can appear as operators in the AST type system.
 *
 * <p>Implementations include primitive {@link Type} values and structured operator
 * types such as {@link BasicType}, {@link CiphertextType}, and {@link FormatType}.
 * The static helper methods provide common runtime type resolution and type
 * equality checks for those implementations.</p>
 */
public interface Operators {

    /**
     * Resolves an operator to the runtime-visible {@link Type} it represents.
     *
     * <p>Primitive {@link Type} values resolve to themselves. Wrapper and
     * structured operator types resolve to their corresponding primitive runtime
     * category.</p>
     *
     * @param operator the operator to resolve
     * @return the runtime-visible type represented by {@code operator}
     * @throws RuntimeException if {@code operator} is not a recognized
     *                          {@code Operators} implementation
     */
    static Type runtimeType(Operators operator) {
        if (operator instanceof Type type) {
            return type;
        }

        if (operator instanceof BasicType basicType) {
            return basicType.type;
        }

        if (operator instanceof CiphertextType) {
            return Type.CIPHERTEXT;
        }

        if (operator instanceof FormatType) {
            return Type.FORMAT;
        }

        if (operator instanceof ClassType) {
            return Type.OBJECT;
        }

        throw new RuntimeException("Unknown operator type: " + operator);
    }

    /**
     * Determines whether two operators represent the same type.
     *
     * <p>{@link CiphertextType} and {@link FormatType} values are compared using
     * their implementation-specific equality rules. Other operator values are
     * compared by their resolved runtime-visible {@link Type}.</p>
     *
     * @param left the first operator to compare
     * @param right the second operator to compare
     * @return {@code true} when both operators represent the same type;
     *         {@code false} when either value is {@code null} or the resolved
     *         types differ
     */
    static boolean sameType(Operators left, Operators right) {
        if (left == right) {
            return true;
        }

        if (left == null || right == null) {
            return false;
        }

        if (left instanceof CiphertextType leftCipher && right instanceof CiphertextType rightCipher) {
            return leftCipher.equals(rightCipher);
        }

        if (left instanceof FormatType leftFormat && right instanceof FormatType rightFormat) {
            return leftFormat.equals(rightFormat);
        }

        if (left instanceof ClassType leftClass && right instanceof ClassType rightClass) {
            return leftClass.equals(rightClass);
        }

        return runtimeType(left) == runtimeType(right);
    }
}
