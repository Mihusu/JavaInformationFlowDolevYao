package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

public class VarPattern extends ReceivePattern {
    String name;

    public VarPattern(String text) {
        this.name = text;
    }

    public boolean match(Value v, Environment env) {
        env.setVariables(name, v);
        return true;
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        if (!gamma.containsLabel(name)) {
            gamma.putLabel(name, SecLabel.HIGH);
        }

        if (!delta.containsType(name)) {
            delta.putType(name, Type.STRING);
        }
    }

    @Override
    public String compileMatch(CodeGenEnv env, String valueVar) {
        return env.indent() + "Object " + name + " = " + valueVar + ";\n";
    }

    @Override
    public String compile(CodeGenEnv env) {
        return "";
    }
}
