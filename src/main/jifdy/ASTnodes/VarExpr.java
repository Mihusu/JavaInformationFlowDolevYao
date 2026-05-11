package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

public class VarExpr extends Expr {
    public String name;

    public VarExpr(String text) {
        this.name = text;
    }

    public Value eval(Environment env) {
        return env.getVariables(name);
    }

    public Type typecheck(TypeEnv delta, LabelEnv gamma) {
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