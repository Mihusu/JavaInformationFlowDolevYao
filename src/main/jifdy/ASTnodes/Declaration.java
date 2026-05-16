package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;

/**
 * Base class for declarations that introduce bindings in the program environment.
 */
abstract public class Declaration extends Node {
    public abstract void eval(Environment env);

    public abstract void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc);
}
