package frontend.ast.units.stmts;

import frontend.lexer.Token;
import frontend.symbols.GetSymTable;
import frontend.symbols.SymbolTable;

public class ForStmt {
    private LVal lVal;
    private Token assign;
    private Exp exp;

    public ForStmt(LVal lVal, Token assign, Exp exp) {
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
    }

    public ForStmt() {
    }

    public void setlVal(LVal lVal) {
        this.lVal = lVal;
    }

    public void setAssign(Token assign) {
        this.assign = assign;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    @Override
    public String toString() {
        return lVal + "\n" + assign + "\n" + exp + "\n<ForStmt>";
    }

    public void checkError(SymbolTable symbolTable) {
        if (!lVal.checkError(symbolTable)) {
            //error h
            if (symbolTable.getSymbol(lVal.getIdent().getValue()).is("Const")) {
                GetSymTable.addError(lVal.getIdent().getLine(), "h");
            }
        }

        exp.checkError(symbolTable);
    }
}
