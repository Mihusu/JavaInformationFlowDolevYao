package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

import java.util.List;

/**
 * Expression node for invoking a method and optionally consuming its return value.
 */
public class MethodCallExpr extends Expr {
    public String name;
    public List<Expr> args;

    @Override
    public Value eval(Environment env) {
        Value value = invoke(env);

        if (value == null) {
            throw new RuntimeException("Void method used as an expression: " + name);
        }

        return value;
    }

    public void evalAsStatement(Environment env) {
        invoke(env);
    }

    private Value invoke(Environment env) {

        MethodDecl f = env.getMethod(name);

        Environment localEnv = new Environment(env);

        // bind parameters
        for (int i = 0; i < f.params.size(); i++) {
            Param p = f.params.get(i);
            Value v = args.get(i).eval(env);

            localEnv.declare(p.name, v, p.label);
        }

        f.body.eval(localEnv);

        if (f.returnType == null) {
            return null;
        }

        return localEnv.getReturnValue();
    }

    @Override
    public Operators typecheck(TypeEnv delta, LabelEnv gamma) {

        MethodType f = delta.getMethod(name);

        if (args.size() != f.paramTypes.size())
            throw new TypeCheckException("Wrong number of arguments");

        for (int i = 0; i < args.size(); i++) {
            Operators argType = args.get(i).typecheck(delta, gamma);

            if (!Operators.sameType(argType, f.paramTypes.get(i))) {
                throw new TypeCheckException("Argument type mismatch");
            }

            if (f.paramLabels != null) {
                SecLabel argLabel = args.get(i).label(gamma);
                SecLabel paramLabel = f.paramLabels.get(i);

                // SPECIAL CASE: Encryption (EncryptExpr) is a declassification mechanism.
                if (args.get(i) instanceof EncryptExpr && paramLabel == SecLabel.LOW) {
                    // Allow it.
                } else if (!Security.canFlow(argLabel, paramLabel)) {
                    throw new TypeCheckException(
                            "Illegal argument flow in call to " + name + ": "
                                    + argLabel + " -> " + paramLabel
                    );
                }
            }
        }

        return f.returnType;
    }

    @Override
    public SecLabel label(LabelEnv gamma) {

        SecLabel result = SecLabel.LOW;

        // join argument labels
        for (Expr arg : args) {
            result = SecLabel.join(result, arg.label(gamma));
        }

        // get method label
        MethodLabel fLabel = gamma.getMethod(name);

        // include method return label
        result = SecLabel.join(result, fLabel.returnLabel);

        return result;
    }


    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        sb.append(name).append("(");

        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i).compile(env));
            if (i < args.size() - 1) sb.append(", ");
        }

        sb.append(")");

        return sb.toString();
    }
}
