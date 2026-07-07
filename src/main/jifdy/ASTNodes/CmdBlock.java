package ASTNodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

import java.util.List;

/**
 * Represents a block of statements (commands) in the AST.
 */
public class CmdBlock extends Stmt {
    public List<Node> statements;

    @Override
    public void eval(Environment env) {
        for (Node s : statements) {

            if (env.hasReturned) break;

            if (s instanceof VarDecl decl) {
                decl.eval(env);
            } else if (s instanceof Stmt stmt) {
                stmt.eval(env);
            }
        }
    }

    @Override
    public void labelTypeChecker(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {
        for (Node s : statements) {

            if (s instanceof VarDecl v) {
                v.labelTypeChecker(delta, gamma, secLabel);
            }

            else if (s instanceof Stmt stmt) {
                stmt.labelTypeChecker(delta, gamma, secLabel);
            }
        }
    }


    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        for (Node s : statements) {
            sb.append(s.compile(env));
        }

        return sb.toString();
    }
}
