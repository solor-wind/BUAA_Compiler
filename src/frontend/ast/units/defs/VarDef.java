package frontend.ast.units.defs;

import frontend.ast.units.exps.ConstExp;
import frontend.lexer.Token;

public class VarDef {
    private Token ident;
    private Token lbrack;
    private Token rbrack;
    private ConstExp constExp;
    private Token assign;
    private InitVal initVal;

    public VarDef(Token ident) {
        this.ident = ident;
    }

    public void setLbrack(Token lbrack) {
        this.lbrack = lbrack;
    }

    public void setRbrack(Token rbrack) {
        this.rbrack = rbrack;
    }

    public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }

    public void setAssign(Token assign) {
        this.assign = assign;
    }

    public void setInitVal(InitVal initVal) {
        this.initVal = initVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident + "\n");
        if (lbrack != null) {
            sb.append(lbrack + "\n");
            sb.append(constExp + "\n");
            sb.append(rbrack + "\n");
        }
        if (assign != null) {
            sb.append(assign + "\n");
            sb.append(initVal + "\n");
        }
        return sb.toString() + "<VarDef>";
    }
}
