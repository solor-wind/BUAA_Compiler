package frontend.symbols;

import frontend.ast.units.defs.*;

import java.util.TreeMap;

public class GetSymTable {
    private CompUnit compUnit;
    public static SymbolTable root = new SymbolTable(null, null);
    public static int outID = 0;
    private static TreeMap<Integer, String> errors = new TreeMap<>();

    public GetSymTable(CompUnit compUnit, TreeMap<Integer, String> errors) {
        this.compUnit = compUnit;
        this.errors = errors;
    }

    public SymbolTable getSymbolTable() {
        return root;
    }


    public void parse() {
        root.setOutID(++outID);
        compUnit.checkError(root);
        for (Symbol symbol : root.getSymbols()) {
            symbol.setGlobal(true);
        }
    }

    public static void addError(int line, String e) {
        errors.put(line, e);
    }

    public static boolean isError() {
        return !errors.isEmpty();
    }

    public String outError() {
        StringBuilder sb = new StringBuilder();
        for (Integer i : errors.keySet()) {
            sb.append(i + " " + errors.get(i) + "\n");
        }
        return sb.toString();
    }

}
