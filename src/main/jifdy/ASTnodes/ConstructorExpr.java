package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.ArrayList;
import java.util.List;

/**
 * Expression node for constructing structured constructor values from evaluated arguments.
 */
public class ConstructorExpr extends Expr {
    public String name;
    public List<Expr> args;

    public ConstructorExpr(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public Value eval(Environment env) {
        List<Value> values = new ArrayList<>();

        for (Expr arg : args) {
            values.add(arg.eval(env));
        }

        return new ConstructorValue(name, values);
    }

    @Override
    public Type typecheck(TypeEnv delta, LabelEnv gamma) {
        for (Expr arg : args) {
            arg.typecheck(delta, gamma);
        }

        return Type.STRING;
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        SecLabel result = SecLabel.LOW;

        for (Expr arg : args) {
            if (arg.label(gamma) == SecLabel.HIGH) {
                result = SecLabel.HIGH;
            }
        }

        return result;
    }

    @Override
    public String compile(CodeGenEnv env) {
        StringBuilder sb = new StringBuilder();

        sb.append("new ConstructorValue(\"")
                .append(name)
                .append("\", Arrays.asList(");

        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i).compile(env));
            if (i < args.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append("))");

        return sb.toString();
    }
}
