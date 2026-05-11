package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;

abstract public class Stmt extends Node {
    public abstract void eval(Environment env);
    abstract public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel secLabel);
}
