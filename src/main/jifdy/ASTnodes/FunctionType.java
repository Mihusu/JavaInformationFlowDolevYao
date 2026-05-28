package ASTnodes;

import java.util.List;

/**
 * Captures the parameter and return types, along with their security labels, for a function.
 */
public class FunctionType {

    public List<Operators> paramTypes;
    public Operators returnType;
    public List<SecLabel> paramLabels;
    public SecLabel returnLabel;

    public FunctionType(List<Operators> paramTypes, Operators returnType,
                        List<SecLabel> paramLabels, SecLabel returnLabel) {
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.paramLabels = paramLabels;
        this.returnLabel = returnLabel;
    }
}
