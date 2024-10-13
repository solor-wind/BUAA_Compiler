package frontend.ast.units.stmts;

import frontend.ast.units.exps.AddExp;
import frontend.symbols.SymbolTable;

public class Exp {
    private AddExp addExp;

    public Exp(AddExp addExp) {
        this.addExp = addExp;
    }

    @Override
    public String toString() {
        return addExp.toString() + "\n<Exp>";
    }

    public boolean checkError(SymbolTable symbolTable) {
        return addExp.checkError(symbolTable);
    }

    public int evaluate(SymbolTable symbolTable) {
        return addExp.evaluate(symbolTable);
    }

    public String getType() {
        return addExp.getType();
    }
}
