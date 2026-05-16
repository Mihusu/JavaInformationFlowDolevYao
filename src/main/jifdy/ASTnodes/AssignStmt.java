package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

/**
 * Represents an assignment statement in the AST.
 */
public class AssignStmt extends Stmt {
    public String name;
    public Expr expr;

    public AssignStmt(String text, Expr visit) {
        this.name = text;
        this.expr = visit;
    }

    public void eval(Environment env) {
        Value v = expr.eval(env);
        env.setVariables(name, v);
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel currentProcedure) {

        Type rhs = expr.typecheck(delta, gamma);
        Type lhs = delta.getType(name);

        if (rhs != lhs) {
            throw new TypeCheckException("Type mismatch in assignment", lineNumber, name);
        }

        SecLabel exprLabel = expr.label(gamma);

        // explicit flow + implicit flow
        SecLabel effectiveLabel = SecLabel.join(currentProcedure, exprLabel);
        SecLabel varLabel = gamma.getLabel(name);

        // SPECIAL CASE: Encryption (EncryptExpr) is a declassification mechanism.
        if(expr instanceof EncryptExpr && varLabel == SecLabel.LOW) {
            // Allow it.
        } else if (!Security.canFlow(effectiveLabel, varLabel)) {
            throw new TypeCheckException(
                    "Illegal information flow: " +
                            effectiveLabel + " -> " + varLabel,
                    lineNumber,
                    name
            );
        }
    }


    @Override
    public String compile(CodeGenEnv env) {
        return env.indent() + name + " = " + expr.compile(env) + ";\n";
    }
}
