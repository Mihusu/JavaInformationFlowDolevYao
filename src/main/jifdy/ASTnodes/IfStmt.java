package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.List;

public class IfStmt extends Stmt {
    public Expr condition;
    public CmdBlock thenCmdBlock;
    public List<ElseIf> elseIfs;
    public CmdBlock elseCmdBlock;

    public void eval(Environment env) {
        if (((BoolValue) condition.eval(env)).value) {
            thenCmdBlock.eval(env);
            return;
        }

        for (ElseIf e : elseIfs) {
            if (((BoolValue) e.condition.eval(env)).value) {
                e.cmdBlock.eval(env);
                return;
            }
        }

        if (elseCmdBlock != null) elseCmdBlock.eval(env);
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel label) {

        Type condType = condition.typecheck(delta, gamma);

        if (condType != Type.BOOL) {
            throw new TypeCheckException("If condition must be boolean");
        }

        SecLabel condLabel = condition.label(gamma);
        SecLabel newLabel = join(label, condLabel);

        thenCmdBlock.typecheck(delta, gamma, newLabel);

        for (ElseIf e : elseIfs) {
            e.cmdBlock.typecheck(delta, gamma, newLabel);
        }

        if (elseCmdBlock != null) {
            elseCmdBlock.typecheck(delta, gamma, newLabel);
        }
    }

    private SecLabel join(SecLabel a, SecLabel b) {
        return (a == SecLabel.HIGH || b == SecLabel.HIGH)
                ? SecLabel.HIGH
                : SecLabel.LOW;
    }

    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("if (")
                .append(condition.compile(env))
                .append(") {\n");

        env.increaseIndent();
        sb.append(thenCmdBlock.compile(env));
        env.decreaseIndent();

        sb.append(env.indent()).append("}\n");

        for (ElseIf e : elseIfs) {
            sb.append(env.indent())
                    .append("else if (")
                    .append(e.condition.compile(env))
                    .append(") {\n");

            env.increaseIndent();
            sb.append(e.cmdBlock.compile(env));
            env.decreaseIndent();

            sb.append(env.indent()).append("}\n");
        }

        if (elseCmdBlock != null) {
            sb.append(env.indent()).append("else {\n");

            env.increaseIndent();
            sb.append(elseCmdBlock.compile(env));
            env.decreaseIndent();

            sb.append(env.indent()).append("}\n");
        }

        return sb.toString();
    }
}
