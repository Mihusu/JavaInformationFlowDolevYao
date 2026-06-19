package ASTNodes;

/**
 * Shared helpers for converting language types to generated Java code and
 * interpreter default values in case of none valid values were provided.
 */
public final class JavaTypeSupport {
    private JavaTypeSupport() {
    }

    public static String toJavaType(Types type) {
        if (type instanceof ClassType classType) {
            return classType.name;
        }

        return switch (Types.type(type)) {
            case INT -> "int";
            case BOOL -> "boolean";
            case STRING -> "String";
            case CIPHERTEXT -> "EncryptedValue";
            case FORMAT -> "ConstructorValue";
            case OBJECT -> "Object";
        };
    }

    /*
     * Naming convention:
     * - defaultValue returns an interpreter Value object for eval() method across all relevant nodes.
     * - defaultValueExpression returns Java source code for compile() method across all relevant nodes.
     */
    public static Value defaultValue(Types type) {
        return switch (Types.type(type)) {
            case INT -> new IntValue(0);
            case BOOL -> new BoolValue(false);
            case STRING -> new StringValue("");
            case CIPHERTEXT -> new EncryptedValue(null, null, "");
            case FORMAT -> new ConstructorValue("", java.util.List.of());
            case OBJECT -> new ObjectValue("");
        };
    }

    public static String defaultValueExpression(Types type) {
        return switch (Types.type(type)) {
            case INT -> "0";
            case BOOL -> "false";
            case STRING -> "\"\"";
            case CIPHERTEXT -> "new EncryptedValue(new byte[0], \"<empty>\")";
            case FORMAT -> "new ConstructorValue(\"\", Arrays.asList())";
            case OBJECT -> "null";
        };
    }
}
