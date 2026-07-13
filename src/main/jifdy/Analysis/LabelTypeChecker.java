package Analysis;

import ASTNodes.*;

/**
 * Orchestrates the type checking process for the entire program.
 * It uses type and label environments to verify semantic correctness and security properties.
 */
public class LabelTypeChecker {

    private final TypeEnv delta = new TypeEnv();
    private final LabelEnv gamma = new LabelEnv();

    public void check(Program program) {
        program.labelTypeChecker(delta, gamma);
    }
}