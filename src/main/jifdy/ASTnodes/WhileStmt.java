package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Represents a while loop statement in the AST.
 */
public class WhileStmt extends Stmt {
    Expr condition;
    public CmdBlock body;

    public WhileStmt(Expr visit, CmdBlock visit1) {
        this.condition = visit;
        this.body = visit1;
    }

    public void eval(Environment env) {
        while (((BoolValue) condition.eval(env)).value) {
            body.eval(env);
        }
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel label) {

        Type condType = condition.typecheck(delta, gamma);

        if (condType != Type.BOOL) {
            throw new TypeCheckException("While condition must be boolean");
        }

        SecLabel condLabel = condition.label(gamma);
        SecLabel newLabel = SecLabel.join(label, condLabel);

        body.typecheck(delta, gamma, newLabel);
    }


    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("while (")
                .append(condition.compile(env))
                .append(") {\n");

        env.increaseIndent();
        sb.append(body.compile(env));
        env.decreaseIndent();

        sb.append(env.indent()).append("}\n");

        return sb.toString();
    }
}
