package frontend.ast.units.defs;

import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.symbols.*;

public class FuncFParam {
    private Token bType;
    private Token ident;
    private Token lbrack = null;
    private Token rbrack;

    public FuncFParam(Token bType, Token ident) {
        this.bType = bType;
        this.ident = ident;
    }

    public void setLbrack(Token lbrack) {
        this.lbrack = lbrack;
    }

    public void setRbrack(Token rbrack) {
        this.rbrack = rbrack;
    }

    @Override
    public String toString() {
        if (lbrack == null) {
            return bType + "\n" + ident + "\n<FuncFParam>";
        } else {
            return bType + "\n" + ident + "\n" + lbrack + "\n" + rbrack + "\n<FuncFParam>";
        }
    }

    public void checkError(SymbolTable symbolTable) {
        //error b
        if (symbolTable.hasDefined(ident.getValue())) {
            GetSymTable.addError(ident.getLine(), "b");
        } else {
            Symbol symbol;
            String type = bType.is(TokenType.INTTK) ? "Int" : "Char";
            if (lbrack != null) {
                symbol = new ArraySym(ident.getValue(), type + "Array");
            } else {
                symbol = new VarSym(ident.getValue(), type);
            }
            symbolTable.addSymbol(symbol);
            symbolTable.getFuncSym().addArg(symbol);
        }
    }
}
