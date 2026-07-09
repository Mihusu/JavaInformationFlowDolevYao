package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.List;

/**
 * Statement node that evaluates expressions and prints their results line by line.
 */
public class PrintStmt extends Stmt {
    List<Expr> args;

    public PrintStmt(List<Expr> args) {
        this.args = args;
    }

    /**
     * Evaluates each print argument in order and writes the runtime value to
     * standard output.
     */
    public void eval(Environment env) {
        for (Expr expr : args) {
            System.out.println(expr.eval(env));
        }
    }

    @Override
    public void labelTypeChecker(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {
        for (Expr e : args) {
            e.typeChecker(delta, gamma);
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
