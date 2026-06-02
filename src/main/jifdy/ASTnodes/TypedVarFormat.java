package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Pattern node that binds a matched value to a variable, optionally with an explicit type.
 */
public class TypedVarFormat extends Format {

    public String name;
    public Operators type;
    public SecLabel label;

    public TypedVarFormat(String name, Operators type, SecLabel label) {
        this.name = name;
        this.type = type;
        this.label = label;
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        Operators effectiveType = type;

        if (effectiveType == null) {
            effectiveType = delta.containsType(name) ? delta.getType(name) : Type.STRING;
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
    public String compileMatch(CodeGenEnv env, String valueVar) {
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

        env.declareVariable(name);
        return env.indent() + JavaTypeSupport.toJavaType(type) + " " + name + " = " + assignmentValue + ";\n";
    }

    @Override
    public String compile(CodeGenEnv env) {
        return "";
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
