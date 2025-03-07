package frontend.ast.units.stmts;

import frontend.ast.units.exps.AddExp;
import frontend.symbols.SymbolTable;
import ir.value.BasicBlock;
import ir.value.Function;
import ir.value.Value;

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

    public Value genIR(Function function, BasicBlock basicBlock) {
        return addExp.genIR(function, basicBlock);
    }
}
