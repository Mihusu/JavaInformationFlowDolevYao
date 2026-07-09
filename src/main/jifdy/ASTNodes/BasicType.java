package ASTNodes;

/**
 * Type descriptor for primitive source-language values.
 *
 * <p>
 * Basic types wrap the runtime-visible {@link Type} enum values used for
 * integers, booleans, and strings. More structured source types, such as
 * class objects, formats, and ciphertexts, are represented by dedicated
 * subclasses of {@link Types}.
 * </p>
 */
public class BasicType extends Types {

    public Type type;

    public BasicType(Type type) {
        super(type);
        this.type = type;
    }
}
