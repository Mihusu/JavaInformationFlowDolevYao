package ASTNodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;
import Utils.Security;
import Utils.TypeCheckException;

/**
 * Assignment statement for receiver field writes, e.g. obj.field = value.
 */
public class FieldAssignStmt extends Stmt {
    public final Expr receiver;
    public final String fieldName;
    public final Expr expr;

    public FieldAssignStmt(Expr receiver, String fieldName, Expr expr) {
        this.receiver = receiver;
        this.fieldName = fieldName;
        this.expr = expr;
    }

    @Override
    public void eval(Environment env) {
        Value target = receiver.eval(env);
        if (!(target instanceof ObjectValue object)) {
            throw new RuntimeException("Field assignment receiver is not an object: " + fieldName);
        }

        env.setField(object, fieldName, expr.eval(env));
    }

    /**
     * Checks assignment to an object field.
     *
     * <p>
     * Field assignment uses the same two labeling obligations as ordinary
     * assignment: the expression label must flow to the field label, and the
     * current program-counter label must flow to the meet of the field label
     * and the label derived from the field type.
     * </p>
     *
     * @param delta Type environment, including class and inherited field data.
     * @param gamma Label environment.
     * @param currentProcedure Current program-counter label.
     */
    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel currentProcedure) {
        Types receiverType = receiver.typecheck(delta, gamma);
        if (!(receiverType instanceof ClassType classType)) {
            throw new TypeCheckException("Field assignment receiver is not an object: " + fieldName);
        }

        VarDecl field = delta.resolveField(classType.name, fieldName);
        Types rhsType = expr.typecheck(delta, gamma);

        if (!delta.isSubtype(rhsType, field.type)) {
            throw new TypeCheckException("Type mismatch in field assignment", lineNumber, fieldName);
        }

        SecLabel exprLabel = expr.label(gamma);
        if (!Security.canFlow(exprLabel, field.label)) {
            throw new TypeCheckException(
                    "Illegal information flow: " + exprLabel + " -> " + field.label,
                    lineNumber,
                    fieldName
            );
        }

        SecLabel typeLabel = delta.infimumLabel(field.type);
        SecLabel pcBound = SecLabel.infimum(field.label, typeLabel);
        if (!Security.canFlow(currentProcedure, pcBound)) {
            throw new TypeCheckException(
                    "Illegal control-flow label in field assignment: " +
                            currentProcedure + " -> " + pcBound,
                    lineNumber,
                    fieldName
            );
        }
    }

    @Override
    public String compile(CodeGenEnv env) {
        return env.indent() + receiver.compile(env) + "." + fieldName + " = " + expr.compile(env) + ";\n";
    }
}

