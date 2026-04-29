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

        for (Param p : params) {
            paramTypes.add(p.type);
        }

        // register function signature
        delta.putFunction(name, new FunctionType(paramTypes, returnType));

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
        SecLabel inferred = localGamma.getReturnLabel();

        // enforce declared label
        if (!Security.canFlow(inferred, returnLabel)) {
            throw new TypeCheckException(
                    "Function return label too low: " + inferred + " -> " + returnLabel
            );
        }

        // update function label
        gamma.getFunction(name).returnLabel = returnLabel;
    }

    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("public static ")
                .append(returnType == null ? "void" : returnType.toString().toLowerCase())
                .append(" ")
                .append(name)
                .append("() {\n");

        env.increaseIndent();

        sb.append(body.compile(env));

        env.decreaseIndent();

        sb.append(env.indent()).append("}\n");

        return sb.toString();
    }
}
