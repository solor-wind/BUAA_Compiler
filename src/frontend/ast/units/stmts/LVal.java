package frontend.ast.units.stmts;

import frontend.lexer.Token;

public class LVal {
    private Token ident;
    private Token lbrack = null;
    private Exp exp;
    private Token rbrack;

    public LVal(Token ident) {
        this.ident = ident;
    }

    public void setLbrack(Token lbrack) {
        this.lbrack = lbrack;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void setRbrack(Token rbrack) {
        this.rbrack = rbrack;
    }

    @Override
    public String toString() {
        if (lbrack == null) {
            return ident.toString() + "\n<LVal>";
        }
        return ident.toString() + "\n" + lbrack.toString() + "\n"
                + exp.toString() + "\n" + rbrack.toString() + "\n<LVal>";
    }
}
