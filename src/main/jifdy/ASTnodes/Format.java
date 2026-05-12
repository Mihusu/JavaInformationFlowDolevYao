package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

public abstract class Format extends Node {

    public abstract void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel currentProcedure);

    public abstract boolean match(Value value, Environment env);

    public abstract String compileMatch(CodeGenEnv env, String valueVar);

    // A method to check one label or potentially multiple labels, depending on the label checking
    public abstract SecLabel label(LabelEnv gamma);

}