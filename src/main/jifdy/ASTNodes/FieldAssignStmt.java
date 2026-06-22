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

        SecLabel effectiveLabel = SecLabel.join(currentProcedure, expr.label(gamma));
        if (!Security.canFlow(effectiveLabel, field.label)) {
            throw new TypeCheckException(
                    "Illegal information flow: " + effectiveLabel + " -> " + field.label,
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
