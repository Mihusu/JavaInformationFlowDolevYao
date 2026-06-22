package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Base class for receive-side patterns used to destructure and bind incoming values.
 */
public abstract class Format {

    /**
     * Type checks this format pattern.
     *
     * <p>
     * Implementations may introduce new variable bindings into the
     * type and label environments and verify that the structure of
     * the format is valid.
     * </p>
     *
     * @param delta Current type environment.
     * @param gamma Current security label environment.
     * @param currentProcedure Current program counter (pc) label.
     */
    public abstract void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel currentProcedure);

    /**
     * Attempts to match a runtime value against this format pattern.
     *
     * <p>
     * If the match succeeds, variables contained in the pattern may
     * be bound in the runtime environment.
     * </p>
     *
     * @param value Runtime value to match.
     * @param env Current runtime environment.
     * @return {@code true} if the value matches the pattern;
     *         {@code false} otherwise.
     */
    public abstract boolean match(Value value, Environment env);

    /**
     * Generates Java code for matching a runtime value against this
     * format pattern.
     *
     * @param env Code generation environment.
     * @param valueVar Name of the Java variable containing the value
     *                 being matched.
     * @return Generated Java source code.
     */
    public abstract String compile(CodeGenEnv env, String valueVar);

    /**
     * Produces a human-readable representation of this format.
     *
     * <p>
     * Used for debugging, logging, message-flow visualization,
     * and compiler diagnostics.
     * </p>
     *
     * @return String representation of the format.
     */
    public abstract String describe();

    /**
     * Computes the security label associated with this format.
     *
     * <p>
     * For compound formats, implementations may combine multiple
     * labels using the language's label lattice.
     * </p>
     *
     * @param gamma Current label environment.
     * @return The resulting security label of the format.
     */
    public abstract SecLabel label(LabelEnv gamma);
}
