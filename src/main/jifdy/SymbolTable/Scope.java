package SymbolTable;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    private final Map<String, Symbol> symbols = new HashMap<>();

    public void declare(Symbol symbol) {
        if (symbols.containsKey(symbol.name)) {
            throw new RuntimeException("Variable already declared: " + symbol.name);
        }
        symbols.put(symbol.name, symbol);
    }

    public Symbol lookupLocal(String name) {
        return symbols.get(name);
    }

    public boolean contains(String name) {
        return symbols.containsKey(name);
    }
}
