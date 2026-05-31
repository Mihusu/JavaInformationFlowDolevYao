package Analysis;

import java.util.HashMap;
import java.util.Map;

import ASTnodes.MethodType;
import ASTnodes.FormatType;
import ASTnodes.Operators;

/**
 * Manages types during type checking.
 * Keeps track of variable types, method signatures, and return types.
 */
public class TypeEnv {

    private final Map<String, Operators> types = new HashMap<>();
    private final Map<String, MethodType> methods = new HashMap<>();
    private final Map<String, FormatType> formats = new HashMap<>();
    private Operators returnType;

    public TypeEnv() {}

    // variables
    public TypeEnv(TypeEnv other) {
        this.types.putAll(other.types);
        this.methods.putAll(other.methods);
        this.formats.putAll(other.formats);
        this.returnType = other.returnType;
    }

    /**
     * Associates a type with a variable name.
     * @param var The name of the variable.
     * @param type The type to associate.
     */
    public void putType(String var, Operators type) {
        types.put(var, type);
    }

    public void setReturnType(Operators type) {
        this.returnType = type;
    }

    public Operators getReturnType() {
        if (returnType == null)
            throw new TypeCheckException("Return type not set");
        return returnType;
    }

    /**
     * Retrieves the type associated with a variable name.
     * @param name The name of the variable.
     * @return The associated type.
     * @throws TypeCheckException if the variable is not found.
     */
    public Operators getType(String name) {
        if (!types.containsKey(name))
            throw new TypeCheckException("Unknown variable: " + name);
        return types.get(name);
    }

    public boolean containsType(String name) {
        return types.containsKey(name);
    }

    public void putFormat(String name, FormatType formatType) {
        formats.put(name, formatType);
    }

    public FormatType getFormat(String name) {
        if (!formats.containsKey(name)) {
            throw new TypeCheckException("Unknown format: " + name);
        }
        return formats.get(name);
    }

    // methods
    public void putMethod(String name, MethodType type) {
        methods.put(name, type);
    }

    public MethodType getMethod(String name) {
        if (!methods.containsKey(name))
            throw new TypeCheckException("Unknown method: " + name);
        return methods.get(name);
    }
}
