package Analysis;
import ASTnodes.SecLabel;

public class TypeCheckException extends RuntimeException {
    public int lineNumber;
    public String violatingVariable;

    public TypeCheckException(String message) {
        super(message);
    }

    public TypeCheckException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public TypeCheckException(String message, int lineNumber, String violatingVariable) {
        super(message);
        this.lineNumber = lineNumber;
        this.violatingVariable = violatingVariable;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (lineNumber > 0) {
            sb.append("Line ").append(lineNumber).append(": ");
        }
        sb.append(super.getMessage());
        if (violatingVariable != null) {
            sb.append(" (Variable: ").append(violatingVariable).append(")");
        }
        return sb.toString();
    }
}
