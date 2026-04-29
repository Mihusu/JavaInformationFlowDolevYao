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

    }

    @Override
    public String compile(CodeGenEnv env) {
        return env.indent() +
                "System.out.println(" +
                (env) +
                ");\n";
    }
}
