package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Pattern node that binds a matched value to a variable, optionally with an explicit type.
 */
public class TypedVarFormat extends Format {

    public String name;
    public Types type;
    public SecLabel label;

    public TypedVarFormat(String name, Types type, SecLabel label) {
        this.name = name;
        this.type = type;
        this.label = label;
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        Types effectiveType = type;

        if (effectiveType == null) {
            effectiveType = delta.containsType(name) ? delta.getType(name) : new BasicType(Type.STRING);
        }

        // Bind variable with declared or inferred type and label
        delta.putType(name, effectiveType);
        gamma.putLabel(name, label);
    }

    @Override
    public boolean match(Value v, Environment env) {
        env.setVariables(name, v);
        return true;
    }

    @Override
    public String compile(CodeGenEnv env, String valueVar) {
        String assignmentValue;
        if (type == null) {
            assignmentValue = valueVar;
            if (env.isVariableDeclared(name)) {
                return env.indent() + name + " = " + assignmentValue + ";\n";
            }

            env.declareVariable(name);
            return env.indent() + "Object " + name + " = " + assignmentValue + ";\n";
        }

        assignmentValue = "(" + JavaTypeSupport.toJavaType(type) + ") " + valueVar;
        if (env.isVariableDeclared(name)) {
            return env.indent() + name + " = " + assignmentValue + ";\n";
        }

        env.declareVariable(name, type);
        return env.indent() + JavaTypeSupport.toJavaType(type) + " " + name + " = " + assignmentValue + ";\n";
    }

    @Override
    public String describe() {
        return name;
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        return this.label;
    }
}
