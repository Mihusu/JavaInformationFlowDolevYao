package Analysis;

import java.util.HashMap;
import java.util.Map;

import ASTNodes.SecLabel;

/**
 * Manages security labels during type checking.
 * Keeps track of variable labels, method labels, and return labels.
 */
public class LabelEnv {

    private final Map<String, SecLabel> labels = new HashMap<>();
    private final Map<String, MethodLabel> methods = new HashMap<>();
    private final Map<String, SecLabel> cipherPayloadLabels = new HashMap<>();
    private SecLabel returnLabel;
    private SecLabel observedReturnLabel;

    public LabelEnv() {}

    // variables
    public LabelEnv(LabelEnv other) {
        this.labels.putAll(other.labels);
        this.methods.putAll(other.methods);
        this.cipherPayloadLabels.putAll(other.cipherPayloadLabels);
        this.returnLabel = other.returnLabel;
        this.observedReturnLabel = other.observedReturnLabel;
    }

    /**
     * Associates a security label with a variable.
     * @param var The variable name.
     * @param label The security label.
     */
    public void putLabel(String var, SecLabel label) {
        labels.put(var, label);
    }

    /**
     * Retrieves the security label associated with a variable.
     * @param var The variable name.
     * @return The security label.
     * @throws TypeCheckException if the variable is not declared.
     */
    public SecLabel getLabel(String var) {
        if (!labels.containsKey(var))
            throw new TypeCheckException("Label error: variable not declared: " + var);
        return labels.get(var);
    }

    // methods
    public void putMethod(String name, MethodLabel label) {
        methods.put(name, label);
    }

    public MethodLabel getMethod(String name) {
        if (!methods.containsKey(name))
            throw new TypeCheckException("Unknown method: " + name);
        return methods.get(name);
    }

    public void setReturnLabel(SecLabel label) {
        this.returnLabel = label;
        this.observedReturnLabel = null;
    }

    public SecLabel getReturnLabel() {
        if (returnLabel == null)
            throw new TypeCheckException("Return label not set");
        return returnLabel;
    }

    /**
     * Updates the observed return label by joining it with a new label.
     * Used for tracking information flow from conditional branches to the return value.
     * @param l The label to join.
     */
    public void updateReturnLabel(SecLabel l) {
        if (this.observedReturnLabel == null) {
            this.observedReturnLabel = l;
        } else {
            this.observedReturnLabel = SecLabel.join(this.observedReturnLabel, l);
        }
    }

    public SecLabel getObservedReturnLabel() {
        return observedReturnLabel != null ? observedReturnLabel : SecLabel.LOW;
    }
}
