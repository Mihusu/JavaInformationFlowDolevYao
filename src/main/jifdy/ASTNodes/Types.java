package ASTNodes;

/**
 * Concrete base class for source-language type descriptors.
 *
 * <p>This class is the common entry point for the AST type system. Primitive
 * types are represented by {@link BasicType}, declared formats by
 * {@link FormatType}, encrypted values by {@link CiphertextType}, and classes
 * by {@link ClassType}. The {@link Type} enum is only the runtime-visible type
 * category used by checks and generated Java code.</p>
 */
public class Types {
    public Type runtimeType;

    // When a subclass creates a type descriptor, it must specify which runtime type it belongs to
    protected Types(Type runtimeType) {
        this.runtimeType = runtimeType;
    }

    /**
     * Check the runtime {@link Type} of the current expr or statement.
     *
     * @param type the type descriptor to resolve
     * @return the runtime-visible type represented by {@code Types}
     * @throws RuntimeException if {@code Types} is not recognized
     */
    public static Type type(Types type) {
        if (type instanceof BasicType basicType) {
            return basicType.type;
        }

        if (type instanceof CiphertextType) {
            return Type.CIPHERTEXT;
        }

        if (type instanceof FormatType || type.runtimeType == Type.FORMAT) {
            return Type.FORMAT;
        }

        if (type instanceof ClassType || type.runtimeType == Type.OBJECT) {
            return Type.OBJECT;
        }

        throw new RuntimeException("Unknown type descriptor: " + type);
    }

    /**
     * Determines whether two type descriptors represent the same type.
     *
     * @param left the first type descriptor to compare
     * @param right the second type descriptor to compare
     * @return {@code true} when both descriptors represent the same type;
     *         {@code false} when either value is {@code null} or the resolved
     *         types differ
     */
    public static boolean sameType(Types left, Types right) {
        if (left == right) {
            return true;
        }

        if (left == null || right == null) {
            return false;
        }

        if (left instanceof CiphertextType leftCiphertext && right instanceof CiphertextType rightCiphertext) {
            return leftCiphertext.equals(rightCiphertext);
        }

        if (left instanceof FormatType leftFormat && right instanceof FormatType rightFormat) {
            return leftFormat.equals(rightFormat);
        }

        if (left instanceof ClassType leftClass && right instanceof ClassType rightClass) {
            return leftClass.equals(rightClass);
        }

        return type(left) == type(right);
    }
}
