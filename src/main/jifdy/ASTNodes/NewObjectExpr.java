package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Expression node for constructing an object instance.
 */
public class NewObjectExpr extends Expr {
    public final String className;

    public NewObjectExpr(String className) {
        this.className = className;
    }

    @Override
    public Value eval(Environment env) {
        return env.instantiate(className);
    }

    @Override
    public Types typecheck(TypeEnv delta, LabelEnv gamma) {
        delta.getClassDecl(className);
        return new ClassType(className);
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        return SecLabel.LOW;
    }

    @Override
    public String compile(CodeGenEnv env) {
        return "new " + className + "()";
    }
}
