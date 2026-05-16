package ASTnodes;

import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Base class for all nodes in the Abstract Syntax Tree (AST).
 */
public abstract class Node {
    /**
     * Line number in the source code where this node was found.
     */
    public int lineNumber;

    /**
     * Compiles the AST node into Java source code.
     * @param env The code generation environment.
     * @return The compiled Java code as a String.
     */
    public abstract String compile(CodeGenEnv env);
}
