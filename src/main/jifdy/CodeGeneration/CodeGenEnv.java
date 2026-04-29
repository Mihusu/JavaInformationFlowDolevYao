package CodeGeneration;
import java.util.*;

public class CodeGenEnv {

    private int tempCounter = 0;
    private int indent = 0;

    private final StringBuilder declarations = new StringBuilder();

    public String freshVar(String prefix) {
        return prefix + "_" + (tempCounter++);
    }

    public String indent() {
        return "    ".repeat(indent);
    }

    public void increaseIndent() { indent++; }
    public void decreaseIndent() { indent--; }

    public void declare(String code) {
        declarations.append(code).append("\n");
    }

    public String getDeclarations() {
        return declarations.toString();
    }
}
