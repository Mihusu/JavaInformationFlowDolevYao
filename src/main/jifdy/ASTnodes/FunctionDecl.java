package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

import java.util.ArrayList;
import java.util.List;

public class FunctionDecl extends Declaration {
    public Privacy privacy;
    public Type returnType; // null = void
    public String name;
    public List<Param> params;
    public CmdBlock body;
    public SecLabel returnLabel;


    @Override
    public void eval(Environment env) {
        env.putFunction(name, this);
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {

        List<Type> paramTypes = new ArrayList<>();
        List<SecLabel> paramLabels = new ArrayList<>();

        for (Param p : params) {
            paramTypes.add(p.type);
            paramLabels.add(p.label);
        }

        // register function signature
        delta.putFunction(name, new FunctionType(paramTypes, returnType, paramLabels, returnLabel));
        gamma.putFunction(name, new FunctionLabel(paramLabels, returnLabel));

        // new scope
        TypeEnv localDelta = new TypeEnv(delta);
        LabelEnv localGamma = new LabelEnv(gamma);

        // return info
        localDelta.setReturnType(returnType);
        localGamma.setReturnLabel(returnLabel);

        // parameters
        for (Param p : params) {
            localDelta.putType(p.name, p.type);
            localGamma.putLabel(p.name, p.label);
        }

        // body (pc ALWAYS LOW at entry)
        body.typecheck(localDelta, localGamma, SecLabel.LOW);

        // inferred return label
        SecLabel inferred = localGamma.getObservedReturnLabel();

        // enforce declared label
        if (!Security.canFlow(inferred, returnLabel)) {
            throw new TypeCheckException(
                    "Function return label too low: " + inferred + " -> " + returnLabel
            );
        }

        // update function label after checking the body
        gamma.getFunction(name).returnLabel = returnLabel;
    }

    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("public ")
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

        sb.append(body.compile(env));

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
        };
    }
}
