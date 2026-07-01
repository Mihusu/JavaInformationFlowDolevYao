package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Utils.TypeCheckException;
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
    public Types typecheck(TypeEnv delta, LabelEnv gamma) {

        Types leftType = left.typecheck(delta, gamma);
        Types rightType = right.typecheck(delta, gamma);
        Type t1 = Types.type(leftType);
        Type t2 = Types.type(rightType);

        switch (op) {
            case "+" -> {
                if (t1 == Type.STRING || t2 == Type.STRING) {
                    return new BasicType(Type.STRING);
                }

                if (t1 != Type.INT || t2 != Type.INT)
                    throw new TypeCheckException("Arithmetic needs INT");
                return new BasicType(Type.INT);
            }
            case "-", "*", "/", "%", "^" -> {
                if (t1 != Type.INT || t2 != Type.INT)
                    throw new TypeCheckException("Arithmetic needs INT");
                return new BasicType(Type.INT);
            }
            case ">", "<", ">=", "<=" -> {
                if (t1 != Type.INT || t2 != Type.INT)
                    throw new TypeCheckException("Comparison needs INT");
                return new BasicType(Type.BOOL);
            }
            case "==", "!=" -> {
                if (!Types.sameType(leftType, rightType))
                    throw new TypeCheckException("Type mismatch in equality");
                return new BasicType(Type.BOOL);
            }
            case "&&", "||" -> {
                if (t1 != Type.BOOL || t2 != Type.BOOL)
                    throw new TypeCheckException("Logical needs BOOL");
                return new BasicType(Type.BOOL);
            }
        }

        throw new RuntimeException("Unknown op: " + op);
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        SecLabel l1 = left.label(gamma);
        SecLabel l2 = right.label(gamma);

        return SecLabel.supremum(l1, l2);
    }

    public Value eval(Environment env) {
        Value l = left.eval(env);
        Value r = right.eval(env);

        switch (op) {
            case "+" -> {
                if (l instanceof StringValue || r instanceof StringValue) {
                    return new StringValue(valueToString(l) + valueToString(r));
                }
                return new IntValue(((IntValue) l).value + ((IntValue) r).value);
            }
            case "-" -> {
                return new IntValue(((IntValue) l).value - ((IntValue) r).value);
            }
            case "*" -> {
                return new IntValue(((IntValue) l).value * ((IntValue) r).value);
            }
            case "/" -> {
                return new IntValue(((IntValue) l).value / ((IntValue) r).value);
            }
            case "%" -> {
                return new IntValue(((IntValue) l).value % ((IntValue) r).value);
            }
            case "^" -> {
                int leftVal = ((IntValue) l).value;
                int rightVal = ((IntValue) r).value;
                return new IntValue((int) Math.pow(leftVal, rightVal));
            }


            // Comparisons
            case ">" -> {
                return new BoolValue(((IntValue) l).value > ((IntValue) r).value);
            }
            case "<" -> {
                return new BoolValue(((IntValue) l).value < ((IntValue) r).value);
            }
            case ">=" -> {
                return new BoolValue(((IntValue) l).value >= ((IntValue) r).value);
            }
            case "<=" -> {
                return new BoolValue(((IntValue) l).value <= ((IntValue) r).value);
            }


            // Equality
            case "==" -> {
                return new BoolValue(l.equals(r));
            }
            case "!=" -> {
                return new BoolValue(!l.equals(r));
            }


            // Logical
            case "and", "&&" -> {
                return new BoolValue(((BoolValue) l).value && ((BoolValue) r).value);
            }
            case "or", "||" -> {
                return new BoolValue(((BoolValue) l).value || ((BoolValue) r).value);
            }
        }

        throw new TypeCheckException("Unknown operator: " + op);
    }

    @Override
    public String compile(CodeGenEnv env) {
        return left.compile(env) + " " + op + " " + right.compile(env);
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
}

