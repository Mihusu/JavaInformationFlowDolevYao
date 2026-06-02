package CodeGeneration;
import java.util.*;

/**
 * Mutable helper state used while lowering AST nodes to the Java source.
 */
public class CodeGenEnv {

    private int tempCounter = 0;
    private int indent = 0;
    private final Deque<Set<String>> variableScopes = new ArrayDeque<>();

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

        if(variableScopes.peek() != null) {
            variableScopes.peek().add(name);
        }
    }

    public boolean isVariableDeclared(String name) {
        for (Set<String> scope : variableScopes) {
            if (scope.contains(name)) {
                return true;
            }
        }

        return false;
    }
}
