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

        Operators lhsType = delta.getType(name);
        Operators rhsType = expr.typecheck(delta, gamma);

        if (!Operators.sameType(lhsType, rhsType)) {
            throw new TypeCheckException("Type mismatch in assignment", lineNumber, name);
        }

        SecLabel exprLabel = expr.label(gamma);

        // explicit flow + implicit flow. effectiveLabel is l2 from the paper
        SecLabel effectiveLabel = SecLabel.join(currentProcedure, exprLabel);
        SecLabel varLabel = gamma.getLabel(name);

        // SPECIAL CASE: Encryption (EncryptExpr) is a declassification mechanism.
        // expr instanceof EncryptExpr && varLabel == SecLabel.LOW "continue" and if the following:
        if(!Security.canFlow(effectiveLabel, varLabel)) {
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
