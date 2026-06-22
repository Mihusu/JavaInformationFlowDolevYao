package Utils;

import org.antlr.v4.runtime.*;

/**
 * Parser error listener that converts ANTLR syntax errors into exceptions for this project.
 */
public class ThrowingErrorListener extends BaseErrorListener {

    public static final ThrowingErrorListener INSTANCE =
            new ThrowingErrorListener();

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int column,
            String msg,
            RecognitionException e
    ) {
        throw new SyntaxError(line, column, msg);
    }
}