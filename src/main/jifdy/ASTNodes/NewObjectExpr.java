package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;
import Utils.TypeCheckException;

import java.util.ArrayList;
import java.util.List;

/**
 * Expression node for constructing an object instance.
 *
 * <p>
 * Object creation resolves the target class and, when a constructor is present,
 * delegates argument type and label validation to the constructor declaration.
 * The resulting expression is labeled by joining the labels of all constructor
 * arguments.
 * </p>
 */
public class NewObjectExpr extends Expr {
    public final String className;
    public final List<Expr> args;

    public NewObjectExpr(String className, List<Expr> args) {
        this.className = className;
        this.args = args;
    }

    @Override
    public Value eval(Environment env) {
        List<Value> values = new ArrayList<>();
        for (Expr arg : args) {
            values.add(arg.eval(env));
        }
        return env.instantiate(className, values);
    }

    @Override
    public Types typeChecker(TypeEnv delta, LabelEnv gamma) {
        ClassDecl classDecl = delta.getClassDecl(className);
        ConstructorDecl constructor = classDecl.getConstructor();

        if (constructor == null) {
            if (!args.isEmpty()) {
                throw new TypeCheckException(
                        "Class " + className + " has no declared constructor"
                );
            }
        } else {
            constructor.checkArguments(args, delta, gamma);
        }

        return new ClassType(className);
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        SecLabel result = SecLabel.LOW;
        for (Expr arg : args) {
            result = SecLabel.supremum(result, arg.label(gamma));
        }
        return result;
    }

    @Override
    public String compile(CodeGenEnv env) {
        StringBuilder sb = new StringBuilder("new ")
                .append(className)
                .append("(");

        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i).compile(env));
            if (i < args.size() - 1) {
                sb.append(", ");
            }
        }

        return sb.append(")").toString();
    }
}

