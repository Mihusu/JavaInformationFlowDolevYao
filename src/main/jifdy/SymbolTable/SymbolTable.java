package SymbolTable;

import java.util.Stack;

public class SymbolTable {

    private final Stack<Scope> scopes = new Stack<>();

    public void enterScope() {
        scopes.push(new Scope());
    }

    public void exitScope() {
        scopes.pop();
    }

    public void declare(Symbol symbol) {
        if (scopes.isEmpty()) {
            throw new RuntimeException("No scope available");
        }
        scopes.peek().declare(symbol);
    }

    public Symbol lookup(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Symbol s = scopes.get(i).lookupLocal(name);
            if (s != null) return s;
        }
        throw new RuntimeException("Undefined variable: " + name);
    }
}