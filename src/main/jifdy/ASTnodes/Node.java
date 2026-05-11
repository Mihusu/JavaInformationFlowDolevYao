package ASTnodes;

import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

public abstract class Node {
    public abstract String compile(CodeGenEnv env);
}
