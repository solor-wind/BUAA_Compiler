package frontend.ast.units.defs;

import frontend.lexer.Token;

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
}
