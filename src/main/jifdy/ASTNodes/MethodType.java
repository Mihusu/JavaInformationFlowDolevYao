package ASTNodes;

import java.util.List;

/**
 * Captures the parameter and return types, along with their security labels, for a method.
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
