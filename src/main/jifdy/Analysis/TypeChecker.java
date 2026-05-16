package Analysis;

import ASTnodes.*;

/**
 * Orchestrates the type checking process for the entire program.
 * It uses type and label environments to verify semantic correctness and security properties.
 */
public class TypeChecker {

    private final TypeEnv delta = new TypeEnv();
    private final LabelEnv gamma = new LabelEnv();

    /**
     * Performs type checking on the provided program.
     * @param program The program AST to check.
     */
    public void check(Program program) {
        for (ClassDecl c : program.classes) {
            c.typecheck(delta, gamma);
        }
    }
}