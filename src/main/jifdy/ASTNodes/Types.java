package ASTNodes;

import java.util.Objects;

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
    public String keyName;
    public String formatName;

    protected Types(Type runtimeType) {
        this.runtimeType = runtimeType;
    }

    /**
     * Convenience constructor for encrypted type descriptors.
     *
     * @param keyName the key name used by the ciphertext type
     * @param formatName the encrypted format name
     */
    public Types(String keyName, String formatName) {
        this.runtimeType = Type.CIPHERTEXT;
        this.keyName = keyName;
        this.formatName = formatName;
    }

    /**
     * Check the runtime {@link Type} represented by a type descriptor.
     *
     * @param typeDescriptor the type descriptor to resolve
     * @return the runtime-visible type represented by {@code typeDescriptor}
     * @throws RuntimeException if {@code typeDescriptor} is not recognized
     */
    public static Type type(Types typeDescriptor) {
        if (typeDescriptor instanceof BasicType basicType) {
            return basicType.type;
        }

        if (typeDescriptor instanceof CiphertextType || typeDescriptor.runtimeType == Type.CIPHERTEXT) {
            return Type.CIPHERTEXT;
        }

        if (typeDescriptor instanceof FormatType || typeDescriptor.runtimeType == Type.FORMAT) {
            return Type.FORMAT;
        }

        if (typeDescriptor instanceof ClassType || typeDescriptor.runtimeType == Type.OBJECT) {
            return Type.OBJECT;
        }

        throw new RuntimeException("Unknown type descriptor: " + typeDescriptor);
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

        if (type(left) == Type.CIPHERTEXT && type(right) == Type.CIPHERTEXT) {
            return Objects.equals(left.keyName, right.keyName)
                    && Objects.equals(left.formatName, right.formatName);
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
