package CodeGeneration;
import java.util.*;

public class CodeGenEnv {

    private int tempCounter = 0;
    private int indent = 0;
    private final Deque<Set<String>> variableScopes = new ArrayDeque<>();

    private final StringBuilder declarations = new StringBuilder();

    public String freshVar(String prefix) {
        return prefix + "_" + (tempCounter++);
    }

    public String indent() {
        return "    ".repeat(indent);
    }

    public void increaseIndent() { indent++; }
    public void decreaseIndent() { indent--; }

    public void pushScope() {
        variableScopes.push(new HashSet<>());
    }

    public void popScope() {
        variableScopes.pop();
    }

    public void declareVariable(String name) {
        if (variableScopes.isEmpty()) {
            pushScope();
        }

        variableScopes.peek().add(name);
    }

    public boolean isVariableDeclared(String name) {
        for (Set<String> scope : variableScopes) {
            if (scope.contains(name)) {
                return true;
            }
        }

        return false;
    }

    public void declare(String code) {
        declarations.append(code).append("\n");
    }

    public String getDeclarations() {
        return declarations.toString();
    }
}
