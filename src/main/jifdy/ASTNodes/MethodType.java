package ASTNodes;

import java.util.List;

/**
 * Captures the parameter and return types, along with their security labels, for a method.
 */
public class MethodType {

    public List<Operators> paramTypes;
    public Operators returnType;
    public List<SecLabel> paramLabels;
    public SecLabel returnLabel;

    public MethodType(List<Operators> paramTypes, Operators returnType,
                        List<SecLabel> paramLabels, SecLabel returnLabel) {
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.paramLabels = paramLabels;
        this.returnLabel = returnLabel;
    }
}
