package Utils;

public class SyntaxError extends RuntimeException {

    private final int line;
    private final int column;

    public SyntaxError(
            int line,
            int column,
            String msg
    ) {
        super(msg);

        this.line = line;
        this.column = column;
    }

    @Override
    public String getMessage() {
        return "Syntax error at line "
                + line
                + ":"
                + column
                + " - "
                + super.getMessage();
    }
}