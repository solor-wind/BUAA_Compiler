package frontend.symbols;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class SymbolTable {
    private SymbolTable parent;
    private FuncSym funcSym;//属于哪个函数
    private LinkedList<SymbolTable> childrens = new LinkedList<>();
    private int outID = 0;

    private LinkedList<Symbol> symbols = new LinkedList<>();
    private HashMap<String, Symbol> hashSymbols = new HashMap<>();


    public SymbolTable(SymbolTable parent, FuncSym funcSym) {
        this.parent = parent;
        this.funcSym = funcSym;
    }

    public SymbolTable(SymbolTable parent, int outID) {
        this.parent = parent;
        this.outID = outID;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public FuncSym getFuncSym() {
        return funcSym;
    }

    public void addChild(SymbolTable child) {
        childrens.add(child);
    }

    public LinkedList<SymbolTable> getChildren() {
        return childrens;
    }

    public void addSymbol(Symbol symbol) {
        symbols.add(symbol);
        hashSymbols.put(symbol.getName(), symbol);
        GetSymTable.symMap.put(getKeyToIR(symbol.getName()), symbol);
    }

    public Symbol getSymbol(String name) {
        if (hashSymbols.containsKey(name)) {
            return hashSymbols.get(name);
        } else if (parent != null) {
            return parent.getSymbol(name);
        }
        return null;
    }

    public String getKeyToIR(String name) {
        if (hashSymbols.containsKey(name)) {
            return outID + name;
        } else if (parent != null) {
            return parent.getKeyToIR(name);
        }
        return null;
    }


    public LinkedList<Symbol> getSymbols() {
        return symbols;
    }

    public int getOutID() {
        return outID;
    }

    public void setOutID(int outID) {
        this.outID = outID;
    }

    public boolean hasDefined(String name) {
        return hashSymbols.containsKey(name);
    }

    public boolean hasDefinedTill(String name) {
        if (hashSymbols.containsKey(name)) {
            return true;
        } else if (parent != null) {
            return parent.hasDefinedTill(name);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Symbol s : symbols) {
            if (s instanceof FuncSym && s.getName().equals("main")) {
                continue;
            }
            sb.append(outID + " " + s.getName() + " " + s.getType() + "\n");
        }
        for (SymbolTable child : childrens) {
            sb.append(child.toString());
        }
        return sb.toString();
    }
}
