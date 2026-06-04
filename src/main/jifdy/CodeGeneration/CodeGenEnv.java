package CodeGeneration;
import java.util.*;

/**
 * Mutable helper state used while lowering AST nodes to the Java source.
 */
public class CodeGenEnv {

    private int tempCounter = 0;
    private int indent = 0;
    private final Deque<Map<String, ASTNodes.Operators>> variableScopes = new ArrayDeque<>();

    public String freshVar(String prefix) {
        return prefix + "_" + (tempCounter++);
    }

    public String indent() {
        return "    ".repeat(indent);
    }

    public void increaseIndent() { indent++; }
    public void decreaseIndent() { indent--; }

    public void pushScope() {
        variableScopes.push(new HashMap<>());
    }

    public void popScope() {
        variableScopes.pop();
    }

    public void declareVariable(String name) {
        declareVariable(name, null);
    }

    public void declareVariable(String name, ASTNodes.Operators type) {
        if (variableScopes.isEmpty()) {
            pushScope();
        }

        if(variableScopes.peek() != null) {
            variableScopes.peek().put(name, type);
        }
    }

    public boolean isVariableDeclared(String name) {
        for (Map<String, ASTNodes.Operators> scope : variableScopes) {
            if (scope.containsKey(name)) {
                return true;
            }
        }

        return false;
    }

    public ASTNodes.Operators getVariableType(String name) {
        for (Map<String, ASTNodes.Operators> scope : variableScopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }

        return null;
    }
}
