package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

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
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        Type rhs = expr.typecheck(delta, gamma);
        Type lhs = delta.getType(name);

        if (rhs != lhs) {
            throw new TypeCheckException("Type mismatch in assignment");
        }

        SecLabel exprLabel = expr.label(gamma);

        // explicit flow + implicit flow
        SecLabel effectiveLabel = SecLabel.join(pc, exprLabel);
        SecLabel varLabel = gamma.getLabel(name);

        if (!Security.canFlow(effectiveLabel, varLabel)) {
            throw new TypeCheckException(
                    "Illegal information flow: " +
                            effectiveLabel + " -> " + varLabel
            );
        }
    }


    @Override
    public String compile(CodeGenEnv env) {
        return env.indent() + name + " = " + expr.compile(env) + ";\n";
    }
}
