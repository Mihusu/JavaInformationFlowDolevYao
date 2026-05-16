package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

/**
 * Represents a variable declaration in the AST.
 */
public class VarDecl extends Declaration {
    /**
     * The type of the variable.
     */
    public Type type;

    /**
     * The security label of the variable.
     */
    public SecLabel label;

    /**
     * The name of the variable.
     */
    public String name;

    /**
     * The optional initializer expression.
     */
    public Expr init; // optional

    /**
     * Evaluates the variable declaration, initializing it in the environment.
     * @param env The execution environment.
     */
    @Override
    public void eval(Environment env) {

        Value initVal;

        if (init != null) {
            initVal = init.eval(env);
        } else {
            initVal = defaultValue(type);
        }

        // assign security label
        initVal.label = label;

        // store in environment
        env.setVariables(name, initVal);
    }

    private Value defaultValue(Type t) {
        switch (t) {
            case INT: return new IntValue(0);
            case BOOL: return new BoolValue(false);
            case STRING: return new StringValue("");
            case CIPHERTEXT: return new EncryptedValue("", new StringValue(""), "");
        }
        throw new RuntimeException("Unknown type");
    }

    /**
     * Performs type checking on the variable declaration.
     * @param delta The type environment.
     * @param gamma The label environment.
     * @param pc The security context (pc).
     */
    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        delta.putType(name, type);
        gamma.putLabel(name, label);

        if (init != null) {

            Type initType = init.typecheck(delta, gamma);

            if (initType != type) {
                throw new TypeCheckException(
                        "Type mismatch in initialization of " + name,
                        lineNumber,
                        name
                );
            }

            SecLabel initLabel = init.label(gamma);
            SecLabel effective = SecLabel.join(pc, initLabel);

            // SPECIAL CASE: Encryption (EncryptExpr) is a declassification mechanism.
            // It results in a LOW ciphertext even if the payload or context is HIGH.
            // The user explicitly requested that encryption doesn't cause illegal flow.
            if (init instanceof EncryptExpr && label == SecLabel.LOW) {
                // Allow it.
            } else if (!Security.canFlow(effective, label)) {
                throw new TypeCheckException(
                        "Illegal flow in initialization of " + name,
                        lineNumber,
                        name
                );
            }
        }
    }


    @Override
    public String compile(CodeGenEnv env) {
        env.declareVariable(name);
        return env.indent() + toJavaType(type) + " " + name + compileInitializer(env) + ";\n";
    }

    public String compileField(CodeGenEnv env) {
        env.declareVariable(name);
        return env.indent() + "public " + toJavaType(type) + " " + name + compileInitializer(env) + ";\n";
    }

    private String compileInitializer(CodeGenEnv env) {
        if (init != null) {
            return " = " + init.compile(env);
        }

        return " = " + defaultJavaValue(type);
    }

    private String toJavaType(Type t) {
        if (t == Type.INT) return "int";
        if (t == Type.BOOL) return "boolean";
        if (t == Type.STRING) return "String";
        if (t == Type.CIPHERTEXT) return "EncryptedValue";
        return "";
    }

    private String defaultJavaValue(Type t) {
        if (t == Type.INT) return "0";
        if (t == Type.BOOL) return "false";
        if (t == Type.STRING) return "\"\"";
        if (t == Type.CIPHERTEXT) return "new EncryptedValue(\"\", \"\", \"\")";
        return "";
    }
}
