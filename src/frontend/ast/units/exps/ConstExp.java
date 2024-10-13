package frontend.ast.units.exps;

import frontend.symbols.SymbolTable;

public class ConstExp {
    public AddExp addExp;

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
    }

    @Override
    public String toString() {
        return addExp + "\n<ConstExp>";
    }

    public boolean checkError(SymbolTable symbolTable) {
        return addExp.checkError(symbolTable);
    }

    public int evaluate(SymbolTable symbolTable) {
        return addExp.evaluate(symbolTable);
    }
}
