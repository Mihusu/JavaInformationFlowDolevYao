package ASTNodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;
import Utils.Security;
import Utils.TypeCheckException;

/**
 * Represents a return statement in the AST.
 */
public class ReturnStmt extends Stmt {
    public Expr returnExpr;

    public ReturnStmt(Expr visit) {
        this.returnExpr = visit;
    }

    @Override
    public void eval(Environment env) {
        Value v = returnExpr.eval(env);
        env.setReturnValue(v);
    }

    @Override
    public void labelTypeChecker(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {

        Types expectedType = delta.getReturnType();

        Types actualType = returnExpr.typeChecker(delta, gamma);

        if (!delta.isSubtype(actualType, expectedType)) {
            throw new TypeCheckException(
                    "Return type mismatch: expected " + expectedType +
                            " but got " + actualType,
                    lineNumber
            );
        }

        SecLabel exprLabel = returnExpr.label(gamma);
        SecLabel actualLabel = SecLabel.supremum(secLabel, exprLabel);

        // Checks the expected secLabel
        SecLabel expectedLabel = gamma.getReturnLabel();

        // SPECIAL CASE: Encryption (EncryptExpr) is a declassification mechanism.
        if (returnExpr instanceof EncryptExpr && expectedLabel == SecLabel.LOW) {
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
                "return " + returnExpr.compile(env) + ";\n";
    }

}

