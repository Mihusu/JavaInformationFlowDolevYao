package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Expression node for binary arithmetic, comparison, equality, and logical operations.
 */
public class OpExpr extends Expr {
    public Expr left;
    public Expr right;
    String op;

    public OpExpr(Expr left, String op, Expr right) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    public Type typecheck(TypeEnv delta, LabelEnv gamma) {

        Type t1 = left.typecheck(delta, gamma);
        Type t2 = right.typecheck(delta, gamma);

        if (op.equals("+")) {
            if (t1 == Type.STRING || t2 == Type.STRING) {
                return Type.STRING;
            }

            if (t1 != Type.INT || t2 != Type.INT)
                throw new TypeCheckException("Arithmetic needs INT");
            return Type.INT;
        }

        if (op.equals("-") || op.equals("*") || op.equals("/") || op.equals("%") || op.equals("^")) {
            if (t1 != Type.INT || t2 != Type.INT)
                throw new TypeCheckException("Arithmetic needs INT");
            return Type.INT;
        }

        if (op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=")) {
            if (t1 != Type.INT || t2 != Type.INT)
                throw new TypeCheckException("Comparison needs INT");
            return Type.BOOL;
        }

        if (op.equals("==") || op.equals("!=")) {
            if (t1 != t2)
                throw new TypeCheckException("Type mismatch in equality");
            return Type.BOOL;
        }

        if (op.equals("&&") || op.equals("||")) {
            if (t1 != Type.BOOL || t2 != Type.BOOL)
                throw new TypeCheckException("Logical needs BOOL");
            return Type.BOOL;
        }

        throw new RuntimeException("Unknown op: " + op);
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        SecLabel l1 = left.label(gamma);
        SecLabel l2 = right.label(gamma);

        return SecLabel.join(l1, l2);
    }

    public Value eval(Environment env) {
        Value l = left.eval(env);
        Value r = right.eval(env);

        if (op.equals("+")) {
            if (l instanceof StringValue || r instanceof StringValue) {
                return new StringValue(valueToString(l) + valueToString(r));
            }
            return new IntValue(((IntValue) l).value + ((IntValue) r).value);
        }

        if (op.equals("-")) {
            return new IntValue(((IntValue) l).value - ((IntValue) r).value);
        }

        if (op.equals("*")) {
            return new IntValue(((IntValue) l).value * ((IntValue) r).value);
        }

        if (op.equals("/")) {
            return new IntValue(((IntValue) l).value / ((IntValue) r).value);
        }

        if (op.equals("%")) {
            return new IntValue(((IntValue) l).value % ((IntValue) r).value);
        }

        if (op.equals("^")) {
            int leftVal = ((IntValue) l).value;
            int rightVal = ((IntValue) r).value;
            return new IntValue((int) Math.pow(leftVal, rightVal));
        }

        // Comparisons
        if (op.equals(">")) {
            return new BoolValue(((IntValue) l).value > ((IntValue) r).value);
        }

        if (op.equals("<")) {
            return new BoolValue(((IntValue) l).value < ((IntValue) r).value);
        }

        if (op.equals(">=")) {
            return new BoolValue(((IntValue) l).value >= ((IntValue) r).value);
        }

        if (op.equals("<=")) {
            return new BoolValue(((IntValue) l).value <= ((IntValue) r).value);
        }

        // Equality
        if (op.equals("==")) {
            return new BoolValue(l.equals(r));
        }

        if (op.equals("!=")) {
            return new BoolValue(!l.equals(r));
        }

        // Logical
        if (op.equals("and") || op.equals("&&")) {
            return new BoolValue(((BoolValue) l).value && ((BoolValue) r).value);
        }

        if (op.equals("or") || op.equals("||")) {
            return new BoolValue(((BoolValue) l).value || ((BoolValue) r).value);
        }

        throw new TypeCheckException("Unknown operator: " + op);
    }

    private String valueToString(Value value) {
        if (value instanceof StringValue) {
            StringValue stringValue = (StringValue) value;
            return stringValue.value;
        }
        if (value instanceof IntValue) {
            IntValue intValue = (IntValue) value;
            return Integer.toString(intValue.value);
        }
        if (value instanceof BoolValue) {
            BoolValue boolValue = (BoolValue) value;
            return Boolean.toString(boolValue.value);
        }
        return value.toString();
    }


    @Override
    public String compile(CodeGenEnv env) {
        return left.compile(env) + " " + op + " " + right.compile(env);
    }
}
