package CodeGeneration;
import ASTNodes.Types;

import java.util.*;

/**
 * Mutable helper state used while lowering AST nodes to the Java source.
 */
public class CodeGenEnv {

    private int tempCounter = 0;
    private int indent = 0;
    private final Deque<Map<String, Types>> variableScopes = new ArrayDeque<>();

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

    public void declareVariable(String name, Types type) {
        if (variableScopes.isEmpty()) {
            pushScope();
        }

        if(variableScopes.peek() != null) {
            variableScopes.peek().put(name, type);
        }
    }

    public boolean isVariableDeclared(String name) {
        for (Map<String, Types> scope : variableScopes) {
            if (scope.containsKey(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a variable is already declared in the innermost scope.
     *
     * <p>
     * Receive code generation uses this to avoid emitting duplicate Java
     * declarations for pattern variables while still allowing names in outer
     * scopes to be shadowed by a receive pattern.
     * </p>
     *
     * @param name Source variable name.
     * @return True when the current scope already contains the name.
     */
    public boolean isVariableDeclaredInCurrentScope(String name) {
        return !variableScopes.isEmpty() && variableScopes.peek().containsKey(name);
    }

    public Types getVariableType(String name) {
        for (Map<String, Types> scope : variableScopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }

        return null;
    }
}
