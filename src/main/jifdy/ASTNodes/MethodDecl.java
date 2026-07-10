package ASTNodes;

import ASTBuilder.PublicPrivateLabel;
import Analysis.*;
import CodeGeneration.CodeGenEnv;
import Utils.Security;
import Utils.TypeCheckException;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a method declaration in the AST.
 *
 * <p>
 * Method declarations introduce a typed and labeled callable unit. During
 * label/type checking, the method signature is registered first, then the body
 * is checked in a local environment containing the parameters and declared
 * return label.
 * </p>
 */
public class MethodDecl extends Declaration {
    public PublicPrivateLabel publicPrivateLabel;
    public Types returnType; // null = void
    public String name;
    public List<Param> params;
    public CmdBlock body;
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
     * @param secLabel The security context of the current procedure.
     */
    @Override
    public void labelTypeChecker(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {

        validateReturnType();

        List<Types> paramTypes = new ArrayList<>();
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
        body.labelTypeChecker(localDelta, localGamma, SecLabel.LOW);

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

    /**
     * Enforces the source-language rule that methods may return only primitive
     * values, while methods with no return value are represented as void.
     */
    private void validateReturnType() {
        if (returnType != null && !(returnType instanceof BasicType)) {
            throw new TypeCheckException(
                    "Invalid return type for method " + name + ": must be a basic type or void"
            );
        }
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
