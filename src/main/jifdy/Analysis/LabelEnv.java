package Analysis;

import java.util.HashMap;
import java.util.Map;

import ASTnodes.SecLabel;

public class LabelEnv {

    private final Map<String, SecLabel> labels = new HashMap<>();
    private final Map<String, FunctionLabel> functions = new HashMap<>();
    private final Map<String, SecLabel> cipherPayloadLabels = new HashMap<>();
    private SecLabel returnLabel;
    private SecLabel observedReturnLabel;

    public LabelEnv() {}

    // variables
    public LabelEnv(LabelEnv other) {
        this.labels.putAll(other.labels);
        this.functions.putAll(other.functions);
        this.cipherPayloadLabels.putAll(other.cipherPayloadLabels);
        this.returnLabel = other.returnLabel;
        this.observedReturnLabel = other.observedReturnLabel;
    }

    public void putLabel(String var, SecLabel label) {
        labels.put(var, label);
    }

    public SecLabel getLabel(String var) {
        if (!labels.containsKey(var))
            throw new TypeCheckException("Label error: variable not declared: " + var);
        return labels.get(var);
    }

    public boolean containsLabel(String var) {
        return labels.containsKey(var);
    }

    // functions
    public void putFunction(String name, FunctionLabel label) {
        functions.put(name, label);
    }

    public FunctionLabel getFunction(String name) {
        if (!functions.containsKey(name))
            throw new TypeCheckException("Unknown function: " + name);
        return functions.get(name);
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

    public void updateReturnLabel(SecLabel l) {
        if (this.observedReturnLabel == null) {
            this.observedReturnLabel = l;
        } else {
            this.observedReturnLabel = join(this.observedReturnLabel, l);
        }
    }

    public SecLabel getObservedReturnLabel() {
        return observedReturnLabel != null ? observedReturnLabel : SecLabel.LOW;
    }

    public void putCipherPayloadLabel(String name, SecLabel label) {
        cipherPayloadLabels.put(name, label);
    }

    public SecLabel getCipherPayloadLabel(String name) {
        return cipherPayloadLabels.get(name);
    }

    private SecLabel join(SecLabel a, SecLabel b) {
        return (a == SecLabel.HIGH || b == SecLabel.HIGH)
                ? SecLabel.HIGH
                : SecLabel.LOW;
    }
}
