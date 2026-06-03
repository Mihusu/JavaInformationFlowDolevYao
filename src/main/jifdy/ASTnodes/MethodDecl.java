package ASTnodes;

import ASTBuilder.Privacy;
import Analysis.*;
import CodeGeneration.CodeGenEnv;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a method declaration in the AST.
 */
public class MethodDecl extends Declaration {
    /**
     * Privacy setting for the method.
     */
    public Privacy privacy;

    /**
     * The return type of the method (null if void).
     */
    public Operators returnType; // null = void

    /**
     * The name of the method.
     */
    public String name;

    /**
     * The list of parameters for the method.
     */
    public List<Param> params;

    /**
     * The body of the method.
     */
    public CmdBlock body;

    /**
     * The declared security label for the method's return value.
     */
    public SecLabel returnLabel;


    /**
     * Registers the method in the execution environment.
     * @param env The execution environment.
     */
    @Override
    public void eval(Environment env) {
        env.putMethod(name, this);
    }

    /**
     * Performs type checking on the method's parameters and body.
     * Verifies that the information flow to the return value respects the declared return label.
     * @param delta The type environment.
     * @param gamma The label environment.
     * @param secLabel The security context (pc).
     */
    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {

        List<Operators> paramTypes = new ArrayList<>();
        List<SecLabel> paramLabels = new ArrayList<>();

        for (Param p : params) {
            paramTypes.add(p.type);
            paramLabels.add(p.label);
        }

        // register method signature
        delta.putMethod(name, new MethodType(paramTypes, returnType, paramLabels, returnLabel));
        gamma.putMethod(name, new MethodLabel(paramLabels, returnLabel));

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

        // body (procedure(method) ALWAYS LOW at entry)
        body.typecheck(localDelta, localGamma, SecLabel.LOW);

        // inferred return label
        SecLabel inferred = localGamma.getObservedReturnLabel();

        // enforce declared label
        if (!Security.canFlow(inferred, returnLabel)) {
            throw new TypeCheckException(
                    "Method return label too low: " + inferred + " -> " + returnLabel
            );
        }

        // update method label after checking the body
        gamma.getMethod(name).returnLabel = returnLabel;
    }

    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("public ")
                .append(returnType == null ? "void" : JavaTypeSupport.toJavaType(returnType))
                .append(" ")
                .append(name)
                .append("(");

        for (int i = 0; i < params.size(); i++) {
            Param p = params.get(i);

            sb.append(JavaTypeSupport.toJavaType(p.type))
                    .append(" ")
                    .append(p.name);

            if (i < params.size() - 1) sb.append(", ");
        }

        sb.append(") {\n");

        env.pushScope();
        for (Param p : params) {
            env.declareVariable(p.name, p.type);
        }

        env.increaseIndent();

        sb.append(body.compile(env));

        env.decreaseIndent();
        env.popScope();

        sb.append(env.indent()).append("}\n");

        return sb.toString();
    }
}
