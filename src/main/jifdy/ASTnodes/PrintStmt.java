package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.List;

public class PrintStmt extends Stmt {
    List<Expr> args;

    public PrintStmt(List<Expr> args) {
        this.args = args;
    }

    public void eval(Environment env) {
        for (Expr e : args) {
            System.out.println(e.eval(env));
        }
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {
        for (Expr e : args) {
            e.typecheck(delta, gamma);
        }
    }

    @Override
    public String compile(CodeGenEnv env) {
        StringBuilder sb = new StringBuilder();

        for (Expr arg : args) {
            sb.append(env.indent())
                    .append("System.out.println(")
                    .append(arg.compile(env))
                    .append(");\n");
        }

        return sb.toString();
    }
}
