package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.Objects;

/**
 * Pattern wrapper for expression-shaped format fields, e.g.
 * `int high amount1 + int high amount2`.
 */
public class ExprFormat extends Format {
    public final Expr expr;

    public ExprFormat(Expr expr) {
        this.expr = expr;
    }

    @Override
    public void labelTypeCheck(TypeEnv delta, LabelEnv gamma) {
        expr.labelTypeCheck(delta, gamma);
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
    public SecLabel label(LabelEnv gamma) {
        return expr.label(gamma);
    }

    @Override
    public String describe() {
        return expr.compile(new CodeGenEnv());
    }
}
