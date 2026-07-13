package Analysis;

import java.util.HashMap;
import java.util.Map;

import ASTNodes.MethodType;
import ASTNodes.FormatType;
import ASTNodes.Types;
import ASTNodes.*;
import Utils.TypeCheckException;

/**
 * Manages types during type checking.
 * Keeps track of variable types, format types, method signatures, and return types.
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

    public void setReturnType(Types type) {
        this.returnType = type;
    }

    public Types getReturnType() {
        if (returnType == null)
            throw new TypeCheckException("Return type not set");
        return returnType;
    }

    // Format types
    public void putFormat(String name, FormatType formatType) {
        validateFormatFieldTypes(formatType);
        formats.put(name, formatType);
    }

    /**
     * Defensively enforces the source-language rule that format declarations
     * may contain only primitive/basic fields or nested format fields.
     */
    private void validateFormatFieldTypes(FormatType formatType) {
        for (Param field : formatType.fields) {
            if (!(field.type instanceof BasicType) && !(field.type instanceof FormatType)) {
                throw new TypeCheckException(
                        "Invalid field type in format " + formatType.name + ": " +
                                field.name + " must be a basic type or declared format type"
                );
            }
        }
    }

    public FormatType getFormat(String name) {
        if (!formats.containsKey(name)) {
            throw new TypeCheckException("Unknown format: " + name);
        }
        return formats.get(name);
    }

    // methods
    public void putMethod(String name, MethodType type) {
        validateMethodReturnType(name, type);
        methods.put(name, type);
    }

    /**
     * Defensively enforces that method declarations return only primitive
     * source values or void.
     */
    private void validateMethodReturnType(String name, MethodType type) {
        if (type.returnType != null && !(type.returnType instanceof BasicType)) {
            throw new TypeCheckException(
                    "Invalid return type for method " + name + ": must be a basic type or void"
            );
        }
    }

    public MethodType getMethod(String name) {
        if (!methods.containsKey(name))
            throw new TypeCheckException("Unknown method: " + name);
        return methods.get(name);
    }

    /**
     * Registers a class declaration.
     *
     * @param cls Class declaration.
     */
    public void putClass(ClassDecl cls) {
        classes.put(cls.name, cls);
    }

    /**
     * Determines whether a class is known.
     *
     * @param name Class name.
     * @return True if the class exists.
     */
    public boolean containsClass(String name) {
        return classes.containsKey(name);
    }

    /**
     * Retrieves a class declaration.
     *
     * @param name Class name.
     * @return Corresponding class declaration.
     * @throws TypeCheckException if the class is unknown.
     */
    public ClassDecl getClassDecl(String name) {
        if (!classes.containsKey(name)) {
            throw new TypeCheckException("Unknown class: " + name);
        }

        return classes.get(name);
    }


    /**
     * Resolves a method declaration using inheritance-aware lookup.
     *
     * @param className Declaring class.
     * @param methodName Method name.
     * @return Resolved method declaration.
     * @throws TypeCheckException if the method cannot be found.
     */
    public MethodDecl resolveMethod(String className, String methodName) {
        ClassDecl cls = getClassDecl(className);
        MethodDecl method = cls.findMethod(methodName, this);
        if (method == null) {
            throw new TypeCheckException("Unknown method " + methodName + " on " + className);
        }
        return method;
    }

    /**
     * Resolves a field declaration using inheritance-aware lookup.
     *
     * @param className Declaring class.
     * @param fieldName Field name.
     * @return Resolved field declaration.
     * @throws TypeCheckException if the field cannot be found.
     */
    public VarDecl resolveField(String className, String fieldName) {
        ClassDecl cls = getClassDecl(className);
        VarDecl field = cls.findField(fieldName, this);
        if (field == null) {
            throw new TypeCheckException("Unknown field " + fieldName + " on " + className);
        }
        return field;
    }

    /**
     * Determines whether one type is a subtype of another.
     *
     * <p>
     * Primitive types, format types, and ciphertext types must match
     * exactly. Class types support inheritance-based subtyping.
     * </p>
     *
     * @param actual Actual type.
     * @param expected Expected type.
     * @return True if actual can be used where expected is required.
     */
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

    /**
     * Computes the label derived from a type, corresponding to the
     * underlined delta/type label used in the DYIF assignment-labeling rule.
     *
     * <p>
     * Most JIFDY types do not carry labels themselves, so they impose no
     * additional restriction beyond the declared variable label. Format types
     * are the exception: their field labels describe the information level of
     * the term, so the derived label is the infimum of the field labels.
     * </p>
     *
     * @param type Source-language type.
     * @return The label derived from the type.
     */
    public SecLabel infimumLabel(Types type) {
        if (!(type instanceof FormatType formatType)) {
            return SecLabel.HIGH;
        }

        SecLabel result = SecLabel.HIGH;
        for (Param field : formatType.fields) {
            result = SecLabel.infimum(result, field.label);
        }

        return result;
    }
}

