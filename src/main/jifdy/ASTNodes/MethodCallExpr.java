package ASTNodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;
import Utils.Security;
import Utils.TypeCheckException;

import java.util.List;

/**
 * Expression node for invoking a method and optionally consuming its return value.
 */
public class MethodCallExpr extends Expr {
    public Expr receiver;
    public String name;
    public List<Expr> args;
    private MethodLabel cachedLabel;

    @Override
    public Value eval(Environment env) {
        Value value = invoke(env);

        if (value == null) {
            throw new RuntimeException("Void method used as an expression: " + name);
        }

        return value;
    }

    @Override
    public Types labelTypeCheck(TypeEnv delta, LabelEnv gamma) {

        MethodType f;
        if (receiver != null) {
            Types receiverType = receiver.labelTypeCheck(delta, gamma);
            if (!(receiverType instanceof ClassType classType)) {
                throw new TypeCheckException("Method call receiver is not an object: " + name);
            }

            MethodDecl method = delta.resolveMethod(classType.name, name);
            f = new MethodType(
                    method.params.stream().map(p -> p.type).toList(),
                    method.returnType,
                    method.params.stream().map(p -> p.label).toList(),
                    method.returnLabel
            );
            cachedLabel = new MethodLabel(f.paramLabels, f.returnLabel);
        } else {
            f = delta.getMethod(name);
            cachedLabel = gamma.getMethod(name);
        }

        if (args.size() != f.paramTypes.size())
            throw new TypeCheckException("Wrong number of arguments");

        for (int i = 0; i < args.size(); i++) {
            Types argType = args.get(i).labelTypeCheck(delta, gamma);

            if (!delta.isSubtype(argType, f.paramTypes.get(i))) {
                throw new TypeCheckException("Argument type mismatch");
            }

            if (f.paramLabels != null) {
                SecLabel argLabel = args.get(i).label(gamma);
                SecLabel paramLabel = f.paramLabels.get(i);

                // SPECIAL CASE: Encryption (EncryptExpr) is a declassification mechanism.
                if (!Security.canFlow(argLabel, paramLabel)) {
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

        if (receiver != null) {
            result = SecLabel.supremum(result, receiver.label(gamma));
        }

        // join argument labels
        for (Expr arg : args) {
            result = SecLabel.supremum(result, arg.label(gamma));
        }

        // get method label
        MethodLabel fLabel = cachedLabel != null ? cachedLabel : gamma.getMethod(name);

        // include method return label
        result = SecLabel.supremum(result, fLabel.returnLabel);

        return result;
    }

    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        if (receiver != null) {
            sb.append(receiver.compile(env)).append(".");
        }

        sb.append(name).append("(");

        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i).compile(env));
            if (i < args.size() - 1) sb.append(", ");
        }

        sb.append(")");

        return sb.toString();
    }

    public void evalAsStatement(Environment env) {
        invoke(env);
    }

    private Value invoke(Environment env) {

        MethodDecl f;
        Environment localEnv = new Environment(env);

        if (receiver != null) {
            Value target = receiver.eval(env);
            if (!(target instanceof ObjectValue object)) {
                throw new RuntimeException("Method call receiver is not an object: " + name);
            }

            f = env.resolveMethod(object.className, name);
            localEnv.setThisObject(object);
        } else {
            f = env.getMethod(name);
        }


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
}

