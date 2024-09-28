package frontend.ast.units.defs;

import frontend.lexer.Token;
import frontend.ast.units.exps.ConstExp;

public class ConstDef {
    private Token ident;//常量名

    private Token lbrack = null;//[
    private ConstExp constExp;
    private Token rbrack = null;//]

    private Token assign;//=
    private ConstInitVal constInitVal;

    public ConstDef(Token ident) {
        this.ident = ident;
    }

    public void setLbrack(Token lbrack) {
        this.lbrack = lbrack;
    }

    public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }

    public void setRbrack(Token rbrack) {
        this.rbrack = rbrack;
    }

    public void setAssign(Token assign) {
        this.assign = assign;
    }

    public void setConstInitVal(ConstInitVal constInitVal) {
        this.constInitVal = constInitVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident + "\n");
        if (lbrack != null) {
            sb.append(lbrack + "\n").append(constExp + "\n").append(rbrack + "\n");
        }
        sb.append(assign + "\n").append(constInitVal + "\n");
        return sb.toString() + "<ConstDef>";
    }
}
