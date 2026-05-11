package Analysis;

import ASTnodes.*;

public class TypeChecker {

    private final TypeEnv delta = new TypeEnv();
    private final LabelEnv gamma = new LabelEnv();

    public void check(Program program) {
        for (ClassDecl c : program.classes) {
            c.typecheck(delta, gamma);
        }
    }
}