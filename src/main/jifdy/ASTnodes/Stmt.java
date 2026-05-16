package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;

/**
 * Represents a statement in the AST.
 */
abstract public class Stmt extends Node {
    /**
     * Executes the statement in the given environment.
     * @param env The execution environment.
     */
    public abstract void eval(Environment env);

    /**
     * Performs type checking on the statement.
     * @param delta The type environment.
     * @param gamma The label environment.
     * @param secLabel The security context (pc) of the statement.
     */
    abstract public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel secLabel);
}
