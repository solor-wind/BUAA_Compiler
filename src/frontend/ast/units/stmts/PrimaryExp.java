package frontend.ast.units.stmts;

import frontend.lexer.Token;

public class PrimaryExp {
    private Token lparent;
    private Exp exp = null;
    private Token rparent;

    private LVal lVal = null;
    private IntConst intConst = null;//+Number
    private CharConst charConst = null;//+Character

    public PrimaryExp() {
    }

    public PrimaryExp(LVal lVal) {
        this.lVal = lVal;
    }

    public PrimaryExp(IntConst intConst) {
        this.intConst = intConst;
    }

    public PrimaryExp(CharConst charConst) {
        this.charConst = charConst;
    }

    public void setLparent(Token lparent) {
        this.lparent = lparent;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void setRparent(Token rparent) {
        this.rparent = rparent;
    }

    @Override
    public String toString() {
        if (exp != null) {
            return lparent + "\n" + exp + "\n" + rparent + "\n<PrimaryExp>";
        } else if (lVal != null) {
            return lVal + "\n<PrimaryExp>";
        } else if (intConst != null) {
            return intConst + "\n<Number>\n<PrimaryExp>";
        } else {
            return charConst + "\n<Character>\n<PrimaryExp>";
        }
    }
}
