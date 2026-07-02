package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;
import Utils.Security;
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

    @Override
    public void labelTypeCheck(TypeEnv delta, LabelEnv gamma) {
        labelTypeCheck(delta, gamma, SecLabel.LOW);
    }

    /**
     * Checks and registers a variable bound by a receive pattern.
     *
     * <p>
     * A receive binding acts like an assignment into the pattern variable. The
     * variable label is therefore checked against the label derived from its
     * type, corresponding to the underlined {@code Delta(x)} side condition in
     * the DYIF assignment-labeling rule.
     * </p>
     *
     * @param delta Type environment updated with the bound variable.
     * @param gamma Label environment updated with the bound variable.
     * @param currentProcedureLabel Current control-flow label.
     */
    @Override
    public void labelTypeCheck(TypeEnv delta, LabelEnv gamma, SecLabel currentProcedureLabel) {

        Types effectiveType = type;
        boolean alreadyDeclared = delta.containsType(name);

        if (effectiveType == null) {
            effectiveType = alreadyDeclared ? delta.getType(name) : new BasicType(Type.STRING);
        }

        if (alreadyDeclared && !delta.isSubtype(effectiveType, delta.getType(name))) {
            throw new TypeCheckException("Receive pattern type mismatch for " + name);
        }

        SecLabel typeLabel = delta.infimumLabel(effectiveType);
        SecLabel allowedProcedureLabel = SecLabel.infimum(label, typeLabel);

        if (!Security.canFlow(currentProcedureLabel, allowedProcedureLabel)) {
            throw new TypeCheckException(
                    "Receive binding for " + name
                            + " is not allowed under control-flow label "
                            + currentProcedureLabel
                            + "; expected flow to "
                            + allowedProcedureLabel
            );
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

    @Override
    public SecLabel label(LabelEnv gamma) {
        return this.label;
    }
}
