package ASTNodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;
import Utils.Security;
import Utils.TypeCheckException;

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

                delta.putType(v.name, v.type);
                gamma.putLabel(v.name, v.label);

                if (v.initExpression != null) {
                    Types t = v.initExpression.labelTypeCheck(delta, gamma);

                    if (!delta.isSubtype(t, v.type)) {
                        throw new TypeCheckException(
                                "Type mismatch in initialization of " + v.name
                        );
                    }

                    SecLabel lExpr = v.initExpression.label(gamma);
                    SecLabel currentLabel = SecLabel.supremum(secLabel, lExpr);

                    // SPECIAL CASE: Encryption (EncryptExpr) is a declassification mechanism.
                    if (v.initExpression instanceof EncryptExpr && v.label == SecLabel.LOW) {
                        // Allow it.
                    } else if (!Security.canFlow(currentLabel, v.label)) {
                        throw new TypeCheckException(
                                "Illegal information flow in initialization of " + v.name
                        );
                    }
                }
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