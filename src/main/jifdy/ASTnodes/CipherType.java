package ASTnodes;

public class CipherType {
    public String operator;
    public Type innerType;

    public CipherType(String operator, Type innerType) {
        this.operator = operator;
        this.innerType = innerType;
    }
}