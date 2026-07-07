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

    /**
     * Checks the DYIF assignment rule for a local variable.
     *
     * <p>
     * The explicit flow check requires the expression label to flow to the
     * variable label. The implicit/control-flow check follows the paper's
     * additional side condition by requiring the current procedure label
     * to flow to the meet of the variable label and the label derived from the
     * variable type.
     * </p>
     *
     * @param delta Type environment.
     * @param gamma Label environment.
     * @param currentProcedureLabel Current procedure label.
     */
    @Override
    public void labelTypeChecker(TypeEnv delta, LabelEnv gamma, SecLabel currentProcedureLabel) {

        Types lhsType = delta.getType(name);
        Types rhsType = assignmentExpr.labelTypeCheck(delta, gamma);

        if (!delta.isSubtype(rhsType, lhsType)) {
            throw new TypeCheckException("Type mismatch in assignment", lineNumber, name);
        }

        SecLabel exprLabel = assignmentExpr.label(gamma);
        SecLabel varLabel = gamma.getLabel(name);
        SecLabel typeLabel = delta.infimumLabel(lhsType);
        SecLabel infinumBound = SecLabel.infimum(varLabel, typeLabel);

        if (!Security.canFlow(exprLabel, varLabel)) {
            throw new TypeCheckException(
                    "Illegal information flow: " + exprLabel + " -> " + varLabel,
                    lineNumber,
                    name
            );
        }

        if (!Security.canFlow(currentProcedureLabel, infinumBound)) {
            throw new TypeCheckException(
                    "Illegal control-flow label in assignment: " +
                            currentProcedureLabel + " -> " + infinumBound,
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

