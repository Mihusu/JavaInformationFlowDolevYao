package ASTNodes;

/**
 * Extracts the symbolic key and message-format names used by ciphertext types.
 *
 * Encryption uses two different AST hierarchies: {@link Expr} represents a
 * payload being encrypted and sent, while {@link Format} represents a pattern
 * used to decrypt and match a received message. The overloaded
 * {@code extractFormatName} methods keep these two cases type-safe while
 * producing the same kind of symbolic format name.
 */
final class EncryptionTypeSupport {

    private EncryptionTypeSupport() {
    }

    static String extractKeyName(Expr keyExpr) {
        if (keyExpr instanceof Expr.StringLiteral literal) {
            return literal.value;
        }

        if (keyExpr instanceof VarExpr varExpr) {
            return varExpr.name;
        }

        throw new RuntimeException("Unsupported encryption key expression");
    }

    /**
     * Extracts a format name from an outgoing payload expression.
     */
    static String extractFormatName(Expr expr) {
        if (expr instanceof ConstructorExpr constructorExpr) {
            return constructorExpr.name;
        }

        if (expr instanceof VarExpr varExpr) {
            return varExpr.name;
        }

        if (expr instanceof EncryptExpr encryptExpr) {
            return encryptExpr.formatName;
        }

        if (expr instanceof Expr.StringLiteral stringLiteral) {
            return stringLiteral.value;
        }

        return expr.getClass().getSimpleName();
    }

    /**
     * Extracts a format name from an incoming receive pattern.
     */
    static String extractFormatName(Format format) {
        if (format instanceof ConstructorFormat constructorFormat) {
            return constructorFormat.name;
        }

        if (format instanceof TypedVarFormat typedVarFormat) {
            return typedVarFormat.name;
        }

        if (format instanceof EncryptFormat encryptFormat) {
            return encryptFormat.ciphertextType().formatName;
        }

        throw new RuntimeException("Unsupported encrypted format pattern");
    }
}
