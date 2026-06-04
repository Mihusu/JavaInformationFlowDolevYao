package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Base class for receive-side patterns used to destructure and bind incoming values.
 */
public abstract class Format extends Node {

    public abstract void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel currentProcedure);

    public abstract boolean match(Value value, Environment env);

    public abstract String compileMatch(CodeGenEnv env, String valueVar);

    // Describe/output the format pattern for the message flow for each
    // sending or receiving message in a print statement to the terminal
    public abstract String describe();

    // A method to check one label or potentially multiple labels, depending on the label checking
    public abstract SecLabel label(LabelEnv gamma);

}
