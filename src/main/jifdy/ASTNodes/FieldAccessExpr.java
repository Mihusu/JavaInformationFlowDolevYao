package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Utils.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Expression node for reading a field through receiver syntax, e.g. obj.field.
 */
public class FieldAccessExpr extends Expr {
    public final Expr receiver;
    public final String fieldName;
    private Types cachedType;
    private SecLabel cachedLabel;

    public FieldAccessExpr(Expr receiver, String fieldName) {
        this.receiver = receiver;
        this.fieldName = fieldName;
    }

    @Override
    public Value eval(Environment env) {
        Value value = receiver.eval(env);
        if (!(value instanceof ObjectValue object)) {
            throw new RuntimeException("Field access receiver is not an object: " + fieldName);
        }

        return env.getField(object, fieldName);
    }

    @Override
    public Types typecheck(TypeEnv delta, LabelEnv gamma) {
        Types receiverType = receiver.typecheck(delta, gamma);
        if (!(receiverType instanceof ClassType classType)) {
            throw new TypeCheckException("Field access receiver is not an object: " + fieldName);
        }

        VarDecl field = delta.resolveField(classType.name, fieldName);
        cachedType = field.type;
        cachedLabel = field.label;
        return cachedType;
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        return SecLabel.supremum(receiver.label(gamma), cachedLabel == null ? SecLabel.LOW : cachedLabel);
    }

    @Override
    public String compile(CodeGenEnv env) {
        return receiver.compile(env) + "." + fieldName;
    }
}

