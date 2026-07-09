package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Expression node that reads the current value of a named variable.
 */
public class VarExpr extends Expr {
    public String name;

    public VarExpr(String text) {
        this.name = text;
    }

    /**
     * Looks up the current runtime value of this variable.
     */
    public Value eval(Environment env) {
        return env.getVariables(name);
    }

    /**
     * Looks up the declared source type of this variable.
     */
    public Types typeChecker(TypeEnv delta, LabelEnv gamma) {
        return delta.getType(name);
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        return gamma.getLabel(name);
    }

    @Override
    public String compile(CodeGenEnv env) {
        return name;
    }
}
