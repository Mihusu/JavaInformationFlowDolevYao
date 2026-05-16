package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Statement wrapper for expressions that are evaluated only for their side effects.
 */
public class FunctionCallStmt extends Stmt {
    public Expr expr;

    public FunctionCallStmt(Expr expr) {
        this.expr = expr;
    }

    @Override
    public void eval(Environment env) {
        if (expr instanceof FunctionCallExpr call) {
            call.evalAsStatement(env);
            return;
        }

        expr.eval(env);
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {
        expr.typecheck(delta, gamma);
    }

    @Override
    public String compile(CodeGenEnv env) {
        return env.indent() + expr.compile(env) + ";\n";
    }
}
