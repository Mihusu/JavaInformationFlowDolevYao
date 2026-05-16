package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a function declaration in the AST.
 */
public class FunctionDecl extends Declaration {
    /**
     * Privacy setting for the function.
     */
    public Privacy privacy;

    /**
     * The return type of the function (null if void).
     */
    public Type returnType; // null = void

    /**
     * The name of the function.
     */
    public String name;

    /**
     * The list of parameters for the function.
     */
    public List<Param> params;

    /**
     * The body of the function.
     */
    public CmdBlock body;

    /**
     * The declared security label for the function's return value.
     */
    public SecLabel returnLabel;


    /**
     * Registers the function in the execution environment.
     * @param env The execution environment.
     */
    @Override
    public void eval(Environment env) {
        env.putFunction(name, this);
    }

    /**
     * Performs type checking on the function's parameters and body.
     * Verifies that the information flow to the return value respects the declared return label.
     * @param delta The type environment.
     * @param gamma The label environment.
     * @param secLabel The security context (pc).
     */
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

        // body (procedure(function) ALWAYS LOW at entry)
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
