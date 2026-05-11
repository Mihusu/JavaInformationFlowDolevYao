package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

public class VarDecl extends Declaration {
    public Type type;
    public SecLabel label;
    public String name;
    public Expr init; // optional

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

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        delta.putType(name, type);
        gamma.putLabel(name, label);

        if (init != null) {

            Type initType = init.typecheck(delta, gamma);

            if (initType != type) {
                throw new TypeCheckException(
                        "Type mismatch in initialization of " + name
                );
            }

            SecLabel initLabel = init.label(gamma);
            SecLabel effective = SecLabel.join(pc, initLabel);

            if (!Security.canFlow(effective, label)) {
                throw new TypeCheckException(
                        "Illegal flow in initialization of " + name
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
        return switch (t) {
            case INT -> "int";
            case BOOL -> "boolean";
            case STRING -> "String";
            case CIPHERTEXT -> "EncryptedValue";
        };
    }

    private String defaultJavaValue(Type t) {
        return switch (t) {
            case INT -> "0";
            case BOOL -> "false";
            case STRING -> "\"\"";
            case CIPHERTEXT -> "new EncryptedValue(\"\", \"\", \"\")";
        };
    }
}
