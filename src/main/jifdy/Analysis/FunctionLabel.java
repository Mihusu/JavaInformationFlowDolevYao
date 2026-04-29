package Analysis;

import ASTnodes.SecLabel;

import java.util.List;

public class FunctionLabel {
    public List<SecLabel> paramLabels;
    public SecLabel returnLabel;

    public FunctionLabel(List<SecLabel> paramLabels, SecLabel returnLabel) {
        this.paramLabels = paramLabels;
        this.returnLabel = returnLabel;
    }
}
