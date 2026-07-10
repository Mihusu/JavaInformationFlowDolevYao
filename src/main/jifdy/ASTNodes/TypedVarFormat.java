package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;
import Utils.TypeCheckException;

import java.util.Map;

/**
 * Pattern node that binds a matched value to a variable, optionally with an explicit type.
 */
public class TypedVarFormat extends Format {

    public String name;
    public Types type;
    public SecLabel label;

    /**
     * Creates a pattern field that binds a matched value to a source variable.
     *
     * @param name Variable name introduced or updated by the pattern.
     * @param type Optional declared source type; inferred when null.
     * @param label Security label assigned to the bound variable.
     */
    public TypedVarFormat(String name, Types type, SecLabel label) {
        this.name = name;
        this.type = type;
        this.label = label;
    }

    /**
     * Checks and registers a variable bound by a receive pattern.
     *
     * <p>
     * A receive binding registers the variables that become available after a
     * successful pattern match. It does not reject the binding merely because
     * the receive occurs under a high control-flow label. Instead, the enclosing
     * {@link TryReceiveStmt} raises the procedure label for the receive body,
     * and later assignments or returns are checked under that raised context.
     * </p>
     *
     * @param delta Type environment updated with the bound variable.
     * @param gamma Label environment updated with the bound variable.
     * @param currentProcedureLabel Current control-flow label, propagated by
     *                              the receive statement but not checked at the
     *                              binding itself.
     */
    @Override
    public void labelTypeChecker(TypeEnv delta, LabelEnv gamma, SecLabel currentProcedureLabel) {

        Types effectiveType = type;
        boolean alreadyDeclared = delta.containsType(name);

        if (effectiveType == null) {
            effectiveType = alreadyDeclared ? delta.getType(name) : new BasicType(Type.STRING);
        }

        if (alreadyDeclared && !delta.isSubtype(effectiveType, delta.getType(name))) {
            throw new TypeCheckException("Receive pattern type mismatch for " + name);
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

    /**
     * Emits Java code that assigns the matched field value to the variable.
     *
     * @param env Code-generation environment.
     * @param valueVar Java expression containing the matched field value.
     * @return Java source fragment for the binding.
     */
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

    /**
     * Records this variable as a binding introduced by the receive pattern.
     *
     * @param bindings output map from variable name to source type.
     */
    @Override
    public void collectBindings(Map<String, Types> bindings) {
        bindings.putIfAbsent(name, type);
    }

    /**
     * Records the security label of this receive-bound variable.
     *
     * @param labels output map from variable name to security label.
     */
    @Override
    public void collectBindingLabels(Map<String, SecLabel> labels) {
        labels.putIfAbsent(name, label);
    }

    @Override
    public String describe() {
        return name;
    }
}
