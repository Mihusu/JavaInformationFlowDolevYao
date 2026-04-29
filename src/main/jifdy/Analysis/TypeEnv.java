package Analysis;

import java.util.HashMap;
import java.util.Map;

import ASTnodes.FunctionType;
import ASTnodes.SecLabel;
import ASTnodes.Type;

public class TypeEnv {

    private final Map<String, Type> types = new HashMap<>();
    private final Map<String, FunctionType> functions = new HashMap<>();
    private final Map<String, Type> cipherPayloadTypes = new HashMap<>();
    private Type returnType;

    public TypeEnv() {}

    // variables
    public TypeEnv(TypeEnv other) {
        this.types.putAll(other.types);
        this.functions.putAll(other.functions);
        this.cipherPayloadTypes.putAll(other.cipherPayloadTypes);
        this.returnType = other.returnType;
    }

    public void putType(String var, Type type) {
        types.put(var, type);
    }

    public void setReturnType(Type type) {
        this.returnType = type;
    }

    public Type getReturnType() {
        if (returnType == null)
            throw new TypeCheckException("Return type not set");
        return returnType;
    }

    public Type getType(String name) {
        if (!types.containsKey(name))
            throw new TypeCheckException("Unknown variable: " + name);
        return types.get(name);
    }

    public boolean containsType(String name) {
        return types.containsKey(name);
    }

    // functions
    public void putFunction(String name, FunctionType type) {
        functions.put(name, type);
    }

    public FunctionType getFunction(String name) {
        if (!functions.containsKey(name))
            throw new TypeCheckException("Unknown function: " + name);
        return functions.get(name);
    }

    public void copyFrom(TypeEnv other) {
        this.types.putAll(other.types);
        this.functions.putAll(other.functions);
        this.cipherPayloadTypes.putAll(other.cipherPayloadTypes);
    }

    public void putCipherPayloadType(String name, Type payloadType) {
        cipherPayloadTypes.put(name, payloadType);
    }

    public Type getCipherPayloadType(String name) {
        return cipherPayloadTypes.get(name);
    }
}
