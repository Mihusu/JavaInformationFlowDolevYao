package Analysis;

import java.util.HashMap;
import java.util.Map;

import ASTNodes.MethodType;
import ASTNodes.FormatType;
import ASTNodes.Types;
import ASTNodes.*;

/**
 * Manages types during type checking.
 * Keeps track of variable types, method signatures, and return types.
 */
public class TypeEnv {

    private final Map<String, Types> types = new HashMap<>();
    private final Map<String, MethodType> methods = new HashMap<>();
    private final Map<String, FormatType> formats = new HashMap<>();
    private final Map<String, ClassDecl> classes = new HashMap<>();
    private Types returnType;

    public TypeEnv() {}

    // variables
    public TypeEnv(TypeEnv other) {
        this.types.putAll(other.types);
        this.methods.putAll(other.methods);
        this.formats.putAll(other.formats);
        this.classes.putAll(other.classes);
        this.returnType = other.returnType;
    }

    /**
     * Associates a type with a variable name.
     * @param var The name of the variable.
     * @param type The type to associate.
     */
    public void putType(String var, Types type) {
        types.put(var, type);
    }

    public void setReturnType(Types type) {
        this.returnType = type;
    }

    public Types getReturnType() {
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
    public Types getType(String name) {
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

    public void putClass(ClassDecl cls) {
        classes.put(cls.name, cls);
    }

    public boolean containsClass(String name) {
        return classes.containsKey(name);
    }

    public ClassDecl getClassDecl(String name) {
        if (!classes.containsKey(name)) {
            throw new TypeCheckException("Unknown class: " + name);
        }

        return classes.get(name);
    }

    public MethodDecl resolveMethod(String className, String methodName) {
        ClassDecl cls = getClassDecl(className);
        MethodDecl method = cls.findMethod(methodName, this);
        if (method == null) {
            throw new TypeCheckException("Unknown method " + methodName + " on " + className);
        }
        return method;
    }

    public VarDecl resolveField(String className, String fieldName) {
        ClassDecl cls = getClassDecl(className);
        VarDecl field = cls.findField(fieldName, this);
        if (field == null) {
            throw new TypeCheckException("Unknown field " + fieldName + " on " + className);
        }
        return field;
    }

    public boolean isSubtype(Types actual, Types expected) {
        if (Types.sameType(actual, expected)) {
            return true;
        }

        if (!(actual instanceof ClassType actualClass) || !(expected instanceof ClassType expectedClass)) {
            return false;
        }

        String current = actualClass.name;
        while (classes.containsKey(current)) {
            ClassDecl cls = classes.get(current);
            if (expectedClass.name.equals(cls.superName)) {
                return true;
            }
            current = cls.superName;
            if (current == null) {
                return false;
            }
        }

        return false;
    }
}
