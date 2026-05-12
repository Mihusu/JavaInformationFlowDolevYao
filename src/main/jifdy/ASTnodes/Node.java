package ASTnodes;

import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

public abstract class Node {
    public int lineNumber;

    public abstract String compile(CodeGenEnv env);
}
