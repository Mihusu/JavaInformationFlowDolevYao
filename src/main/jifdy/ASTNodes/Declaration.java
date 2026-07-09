package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;

/**
 * Base class for declarations that introduce bindings in the program environment.
 */
abstract public class Declaration extends Node {
    /**
     * Applies this declaration to the runtime environment, usually by
     * introducing a variable, method, constructor, or related binding.
     */
    public abstract void eval(Environment env);

    /**
     * Checks the declaration's ordinary type rules and security-label rules
     * under the current declaration/control-flow context.
     */
    public abstract void labelTypeChecker(TypeEnv delta, LabelEnv gamma, SecLabel declarationProcedure);
}
