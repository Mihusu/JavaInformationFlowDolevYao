package ASTNodes;

/**
 * Typed and labeled binding used in signatures and message formats.
 *
 * <p>
 * A parameter describes one name together with its ordinary type and security
 * label. The same shape is reused for method parameters, constructor
 * parameters, and fields inside declared message formats.
 * </p>
 */
public class Param {
    public Types type;
    public String name;
    public SecLabel label;
}
