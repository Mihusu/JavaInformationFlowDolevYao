package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

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

        switch (op) {

            case "+" -> {
                if (t1 == Type.STRING || t2 == Type.STRING) {
                    return Type.STRING;
                }

                if (t1 != Type.INT || t2 != Type.INT)
                    throw new TypeCheckException("Arithmetic needs INT");
                return Type.INT;
            }

            case "-", "*", "/", "%", "^" -> {
                if (t1 != Type.INT || t2 != Type.INT)
                    throw new TypeCheckException("Arithmetic needs INT");
                return Type.INT;
            }

            case ">", "<", ">=", "<=" -> {
                if (t1 != Type.INT || t2 != Type.INT)
                    throw new TypeCheckException("Comparison needs INT");
                return Type.BOOL;
            }

            case "==", "!=" -> {
                if (t1 != t2)
                    throw new TypeCheckException("Type mismatch in equality");
                return Type.BOOL;
            }

            case "&&", "||" -> {
                if (t1 != Type.BOOL || t2 != Type.BOOL)
                    throw new TypeCheckException("Logical needs BOOL");
                return Type.BOOL;
            }

            default -> throw new RuntimeException("Unknown op: " + op);
        }
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

        return switch (op) {
            case "+" -> {
                if (l instanceof StringValue || r instanceof StringValue) {
                    yield new StringValue(valueToString(l) + valueToString(r));
                }

                yield new IntValue(((IntValue) l).value + ((IntValue) r).value);
            }
            case "-" ->
                    new IntValue(((IntValue) l).value - ((IntValue) r).value);

            case "*" ->
                    new IntValue(((IntValue) l).value * ((IntValue) r).value);

            case "/" ->
                    new IntValue(((IntValue) l).value / ((IntValue) r).value);

            case "%" ->
                    new IntValue(((IntValue) l).value % ((IntValue) r).value);

            case "^" -> {
                int leftVal = ((IntValue) l).value;
                int rightVal = ((IntValue) r).value;
                yield new IntValue((int) Math.pow(leftVal, rightVal));
            }

            // Comparisons
            case ">" ->
                    new BoolValue(((IntValue) l).value > ((IntValue) r).value);

            case "<" ->
                    new BoolValue(((IntValue) l).value < ((IntValue) r).value);

            case ">=" ->
                    new BoolValue(((IntValue) l).value >= ((IntValue) r).value);

            case "<=" ->
                    new BoolValue(((IntValue) l).value <= ((IntValue) r).value);

            // Equality
            case "==" ->  new BoolValue(l.equals(r));

            case "!=" ->  new BoolValue(!l.equals(r));

            // Logical
            case "and", "&&" ->
                    new BoolValue(((BoolValue) l).value && ((BoolValue) r).value);

            case "or", "||" ->
                    new BoolValue(((BoolValue) l).value || ((BoolValue) r).value);

            default -> throw new TypeCheckException("Unknown operator: " + op);
        };
    }

    private String valueToString(Value value) {
        if (value instanceof StringValue stringValue) {
            return stringValue.value;
        }
        if (value instanceof IntValue intValue) {
            return Integer.toString(intValue.value);
        }
        if (value instanceof BoolValue boolValue) {
            return Boolean.toString(boolValue.value);
        }
        return value.toString();
    }


    @Override
    public String compile(CodeGenEnv env) {
        return left.compile(env) + " " + op + " " + right.compile(env);
    }
}
