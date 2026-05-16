package Analysis;

import ASTnodes.SecLabel;

import java.util.List;

/**
 * Stores security label information for a function, including parameter labels and return label.
 */
public class FunctionLabel {
    public List<SecLabel> paramLabels;
    public SecLabel returnLabel;

    /**
     * Constructs a FunctionLabel with specified parameter and return labels.
     * @param paramLabels Labels for each parameter.
     * @param returnLabel Label for the return value.
     */
    public FunctionLabel(List<SecLabel> paramLabels, SecLabel returnLabel) {
        this.paramLabels = paramLabels;
        this.returnLabel = returnLabel;
    }
}
