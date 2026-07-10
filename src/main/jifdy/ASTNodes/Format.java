package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.Map;

/**
 * Base class for receive-side patterns used to destructure and bind incoming values.
 */
public abstract class Format {

    /**
     * Type and label checks this format pattern under a current control-flow
     * label.
     *
     * <p>
     * Implementations may introduce new variable bindings into the type and
     * label environments, verify that the pattern structure is valid, and use
     * the current procedure label when the pattern has information-flow side
     * conditions.
     * </p>
     *
     * @param delta Current type environment.
     * @param gamma Current security label environment.
     * @param currentProcedureLabel Current control-flow label.
     */
    public abstract void labelTypeChecker(TypeEnv delta, LabelEnv gamma, SecLabel currentProcedureLabel);

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
     * Collects variables that this receive pattern binds when matching succeeds.
     *
     * <p>
     * Code generation uses this before emitting the Java try/catch so variables
     * bound by a receive pattern also exist after the receive statement. This
     * mirrors the type environment, where receive-bound variables are known
     * after type checking, while still assigning their real values only on a
     * successful match.
     * </p>
     *
     * @param bindings output map from variable name to source type.
     */
    public void collectBindings(Map<String, Types> bindings) {
    }

    /**
     * Collects security labels for variables bound by this receive pattern.
     *
     * <p>
     * The resulting labels are used when default receive variables are
     * declared by the interpreter before matching. They must agree with the
     * labels that pattern type checking adds to the label environment.
     * </p>
     *
     * @param labels output map from variable name to security label.
     */
    public void collectBindingLabels(Map<String, SecLabel> labels) {
    }

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
}
