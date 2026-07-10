package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.Objects;

/**
 * Pattern wrapper for expression-shaped format fields, e.g.
 * `int high amount1 + int high amount2`.
 *
 * <p>
 * Unlike typed receive variables, this pattern does not introduce a binding.
 * It evaluates an ordinary expression and requires the received field to match
 * that value.
 * </p>
 */
public class FormatExpr extends Format {
    public final Expr expr;

    public FormatExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public void labelTypeChecker(TypeEnv delta, LabelEnv gamma, SecLabel currentProcedureLabel) {
        expr.typeChecker(delta, gamma);
    }

    @Override
    public boolean match(Value value, Environment env) {
        // The interpreter compares runtime Value objects here, so primitive Value
        // subclasses must implement equals/hashCode for computed fields to match.
        return Objects.equals(value, expr.eval(env));
    }

    @Override
    public String compile(CodeGenEnv env, String valueVar) {
        // Generated code stores constructor fields as boxed Java primitives/strings,
        // so Objects.equals is the right equality check on the compiled path as well.
        return env.indent()
                + "if (!Objects.equals(" + valueVar + ", " + expr.compile(env) + ")) throw new RuntimeException();\n";
    }

    @Override
    public String describe() {
        return expr.compile(new CodeGenEnv());
    }
}
