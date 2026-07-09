package ASTNodes;

import java.util.List;

/**
 * Type and label signature for a method.
 *
 * <p>
 * Method signatures are stored in the type environment before method bodies
 * are checked. Calls use this descriptor to validate argument types, argument
 * label flows, and the label of the returned expression.
 * </p>
 */
public class MethodType {

    public List<Types> paramTypes;
    public Types returnType;
    public List<SecLabel> paramLabels;
    public SecLabel returnLabel;

    public MethodType(List<Types> paramTypes, Types returnType,
                      List<SecLabel> paramLabels, SecLabel returnLabel) {
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.paramLabels = paramLabels;
        this.returnLabel = returnLabel;
    }
}
