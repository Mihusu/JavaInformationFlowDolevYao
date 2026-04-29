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
                            " but got " + actualType
            );
        }

        SecLabel exprLabel = expr.label(gamma);
        SecLabel actualLabel = join(secLabel, exprLabel);

        gamma.updateReturnLabel(actualLabel);

        // Checks the expected secLabel
        SecLabel expectedLabel = gamma.getReturnLabel();

        if (!Security.canFlow(actualLabel, expectedLabel)) {
            throw new TypeCheckException(
                    "Illegal return flow: " + actualLabel +
                            " -> " + expectedLabel
            );
        }
    }

    @Override
    public String compile(CodeGenEnv env) {
        return env.indent() +
                "return " + expr.compile(env) + ";\n";
    }

    // Helper function
    private SecLabel join(SecLabel a, SecLabel b) {
        return (a == SecLabel.HIGH || b == SecLabel.HIGH)
                ? SecLabel.HIGH
                : SecLabel.LOW;
    }
}
