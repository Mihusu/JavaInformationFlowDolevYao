package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Expression node for unary logical negation and numeric negation.
 */
public class UnaryExpr extends Expr {

    public String op;
    public Expr expr;

    public UnaryExpr(String op, Expr expr) {
        this.op = op;
        this.expr = expr;
    }

    @Override
    public Value eval(Environment env) {

        Value v = expr.eval(env);

        return switch (op) {

            case "!", "not" -> {
                if (!(v instanceof BoolValue b)) {
                    throw new RuntimeException("Expected boolean for NOT");
                }
                yield new BoolValue(!b.value);
            }

            case "-" -> {
                if (!(v instanceof IntValue i)) {
                    throw new RuntimeException("Expected int for unary -");
                }
                yield new IntValue(-i.value);
            }

            default -> throw new RuntimeException("Unknown unary operator: " + op);
        };
    }

    @Override
    public Type typecheck(TypeEnv delta, LabelEnv gamma) {

        Type t = expr.typecheck(delta, gamma);

        return switch (op) {

            case "!", "not" -> {
                if (t != Type.BOOL) {
                    throw new TypeCheckException("NOT requires BOOL");
                }
                yield Type.BOOL;
            }

            case "-" -> {
                if (t != Type.INT) {
                    throw new TypeCheckException("Unary - requires INT");
                }
                yield Type.INT;
            }

            default -> throw new RuntimeException("Unknown unary operator");
        };
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        return expr.label(gamma);
    }

    @Override
    public String compile(CodeGenEnv env) {

        return switch (op) {

            case "not", "!" -> "!" + expr.compile(env);
            case "-" -> "-" + expr.compile(env);

            default -> throw new RuntimeException("Unknown unary operator");
        };
    }
}
