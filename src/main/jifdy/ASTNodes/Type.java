package ASTNodes;

/**
 * Runtime-visible type categories supported by the language.
 *
 * <p>
 * The enum is intentionally coarse: primitive values are represented directly,
 * while structured source types such as classes, formats, and ciphertexts carry
 * their extra metadata in subclasses of {@link Types}.
 * </p>
 */
public enum Type {
    INT, BOOL, STRING, CIPHERTEXT, FORMAT, OBJECT
}
