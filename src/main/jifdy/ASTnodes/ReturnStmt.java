package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

public class ReturnStmt extends Stmt {
    public Expr expr;

    public ReturnStmt(Expr visit) {
        this.expr = visit;
    }

    @Override
    public void eval(Environment env) {
        Value v = expr.eval(env);
        env.setReturnValue(v);
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {

        Type expectedType = delta.getReturnType();

        Type actualType = expr.typecheck(delta, gamma);

        if (actualType != expectedType) {
            throw new TypeCheckException(
                    "Return type mismatch: expected " + expectedType +
                            " but got " + actualType,
                    lineNumber
            );
        }

        SecLabel exprLabel = expr.label(gamma);
        SecLabel actualLabel = SecLabel.join(secLabel, exprLabel);

        // Checks the expected secLabel
        SecLabel expectedLabel = gamma.getReturnLabel();

        // SPECIAL CASE: Encryption (EncryptExpr) is a declassification mechanism.
        if (expr instanceof EncryptExpr && expectedLabel == SecLabel.LOW) {
            // Allow it.
        } else if (!Security.canFlow(actualLabel, expectedLabel)) {
            throw new TypeCheckException(
                    "Illegal return flow: " + actualLabel +
                            " -> " + expectedLabel,
                    lineNumber
            );
        }

        gamma.updateReturnLabel(actualLabel);
    }

    @Override
    public String compile(CodeGenEnv env) {
        return env.indent() +
                "return " + expr.compile(env) + ";\n";
    }

}
