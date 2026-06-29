package ASTNodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;
import Utils.Security;
import Utils.TypeCheckException;

/**
 * Represents an assignment statement in the AST.
 */
public class AssignStmt extends Stmt {
    public String name;
    public Expr assignmentExpr;

    public AssignStmt(String text, Expr visit) {
        this.name = text;
        this.assignmentExpr = visit;
    }

    public void eval(Environment env) {
        Value v = assignmentExpr.eval(env);
        env.setVariables(name, v);
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel currentProcedure) {

        Types lhsType = delta.getType(name);
        Types rhsType = assignmentExpr.typecheck(delta, gamma);

        if (!delta.isSubtype(rhsType, lhsType)) {
            throw new TypeCheckException("Type mismatch in assignment", lineNumber, name);
        }

        SecLabel exprLabel = assignmentExpr.label(gamma);

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
        return env.indent() + name + " = " + assignmentExpr.compile(env) + ";\n";
    }
}
