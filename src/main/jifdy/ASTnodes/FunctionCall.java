package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

import java.util.List;

public class FunctionCall extends Expr {
    public String name;
    public List<Expr> args;

    @Override
    public Value eval(Environment env) {

        FunctionDecl f = env.getFunction(name);

        Environment localEnv = new Environment();

        // bind parameters
        for (int i = 0; i < f.params.size(); i++) {
            Param p = f.params.get(i);
            Value v = args.get(i).eval(env);

            localEnv.setVariables(p.name, v);
        }

        f.body.eval(localEnv);

        return localEnv.getReturnValue();
    }

    @Override
    public Type typecheck(TypeEnv delta, LabelEnv gamma) {

        FunctionType f = delta.getFunction(name);

        if (args.size() != f.paramTypes.size())
            throw new TypeCheckException("Wrong number of arguments");

        for (int i = 0; i < args.size(); i++) {
            Type argType = args.get(i).typecheck(delta, gamma);

            if (argType != f.paramTypes.get(i)) {
                throw new TypeCheckException("Argument type mismatch");
            }
        }

        return f.returnType;
    }

    @Override
    public SecLabel label(LabelEnv gamma) {

        SecLabel result = SecLabel.LOW;

        // join argument labels
        for (Expr arg : args) {
            result = join(result, arg.label(gamma));
            System.out.println("Arg label: " + arg.label(gamma));
        }

        // get function label
        FunctionLabel fLabel = gamma.getFunction(name);

        // include function return label
        result = join(result, fLabel.returnLabel);

        return result;
    }

    private SecLabel join(SecLabel a, SecLabel b) {
        return (a == SecLabel.HIGH || b == SecLabel.HIGH)
                ? SecLabel.HIGH
                : SecLabel.LOW;
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
