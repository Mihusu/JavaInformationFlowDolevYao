package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcDecl extends Declaration {
    public Privacy privacy;
    public String name;
    public Type returnType;
    public SecLabel returnLabel;
    public List<Param> params;
    public List<Stmt> body;

    Map<String, ProcDecl> procedures  = new HashMap<>();

    public void putProcedure(String name, ProcDecl proc) {
        procedures.put(name, proc);
    }

    public ProcDecl getProcedure(String name) {
        if (!procedures.containsKey(name))
            throw new RuntimeException("Unknown procedure: " + name);
        return procedures.get(name);
    }

    @Override
    public void eval(Environment env) {
        env.putProcedure(name, this);
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        // Create local environments (new scope)
        TypeEnv localDelta = new TypeEnv(delta);
        LabelEnv localGamma = new LabelEnv(gamma);

        // Set return type + label
        localDelta.setReturnType(returnType);
        localGamma.setReturnLabel(returnLabel != null ? returnLabel : SecLabel.LOW);

        // Register parameters
        for (Param p : params) {
            localDelta.putType(p.name, p.type);
            localGamma.putLabel(p.name, p.label);
        }

        // Typecheck body
        for (Stmt stmt : body) {
            stmt.typecheck(localDelta, localGamma, SecLabel.LOW);
        }
    }

    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("public static ")
                .append(returnType == null ? "void" : toJavaType(returnType))
                .append(" ")
                .append(name)
                .append("(");

        for (int i = 0; i < params.size(); i++) {
            Param p = params.get(i);

            sb.append(toJavaType(p.type))
                    .append(" ")
                    .append(p.name);

            if (i < params.size() - 1) sb.append(", ");
        }

        sb.append(") {\n");

        env.pushScope();
        for (Param p : params) {
            env.declareVariable(p.name);
        }

        env.increaseIndent();

        for (Stmt stmt : body) {
            sb.append(stmt.compile(env));
        }

        env.decreaseIndent();
        env.popScope();

        sb.append(env.indent()).append("}\n");

        return sb.toString();
    }

    private String toJavaType(Type type) {
        return switch (type) {
            case INT -> "int";
            case BOOL -> "boolean";
            case STRING -> "String";
            case CIPHERTEXT -> "EncryptedValue";
            default -> throw new RuntimeException("Unknown type: " + type);
        };
    }
}
