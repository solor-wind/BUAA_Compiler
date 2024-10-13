package frontend.symbols;

import java.util.LinkedList;

public class FuncSym extends Symbol {
    private LinkedList<Symbol> args = new LinkedList<>();//参数类型

    public FuncSym(String name, String type) {
        super(name, type);
    }

    public void setArgs(LinkedList<Symbol> args) {
        this.args = args;
    }

    public void addArg(Symbol s) {
        args.add(s);
    }

    public LinkedList<Symbol> getArgs() {
        return args;
    }
}
