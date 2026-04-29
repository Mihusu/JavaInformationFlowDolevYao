package ASTnodes;

import java.util.List;

public class FunctionType {

    public List<Type> paramTypes;
    public Type returnType;
    public List<SecLabel> paramLabels;
    public SecLabel returnLabel;

    public FunctionType(List<Type> paramTypes, Type returnType) {
        this(paramTypes, returnType, null, null);
    }

    public FunctionType(List<Type> paramTypes, Type returnType,
                        List<SecLabel> paramLabels, SecLabel returnLabel) {
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.paramLabels = paramLabels;
        this.returnLabel = returnLabel;
    }
}
