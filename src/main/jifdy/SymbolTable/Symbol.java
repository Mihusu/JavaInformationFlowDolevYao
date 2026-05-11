package SymbolTable;

import SymbolTable.types.Type;

import java.awt.*;

public class Symbol {

    public final String name;
    public final Type type;
    public final Label label;

    public Symbol(String name, Type type, Label label) {
        this.name = name;
        this.type = type;
        this.label = label;
    }
}