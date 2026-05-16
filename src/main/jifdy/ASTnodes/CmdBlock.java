package ASTnodes;

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

            if (s instanceof Stmt stmt) {
                stmt.eval(env);
            }
        }
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {
        for (Node s : statements) {

            if (s instanceof VarDecl v) {

                delta.putType(v.name, v.type);
                gamma.putLabel(v.name, v.label);

                if (v.init != null) {
                    Type t = v.init.typecheck(delta, gamma);

                    if (t != v.type) {
                        throw new TypeCheckException(
                                "Type mismatch in initialization of " + v.name
                        );
                    }

                    SecLabel lExpr = v.init.label(gamma);
                    SecLabel currentLabel = SecLabel.join(secLabel, lExpr);

                    // SPECIAL CASE: Encryption (EncryptExpr) is a declassification mechanism.
                    if (v.init instanceof EncryptExpr && v.label == SecLabel.LOW) {
                        // Allow it.
                    } else if (!Security.canFlow(currentLabel, v.label)) {
                        throw new TypeCheckException(
                                "Illegal information flow in initialization of " + v.name
                        );
                    }
                }
            }

            else if (s instanceof Stmt stmt) {
                stmt.typecheck(delta, gamma, secLabel);
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
