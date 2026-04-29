package ASTnodes;

import java.util.List;

public class FunctionType {

    public List<Type> paramTypes;
    public Type returnType;

    public FunctionType(List<Type> paramTypes, Type returnType) {
        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }
}