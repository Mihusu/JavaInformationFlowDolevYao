package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

public abstract class ReceivePattern extends Node {

    public abstract void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc);
    public abstract boolean match(Value value, Environment env);
    abstract String compileMatch(CodeGenEnv env, String valueVar);
}