package frontend.ast.units.stmts;

import frontend.ast.units.exps.AddExp;

public class Exp {
    private AddExp addExp;
    public Exp(AddExp addExp) {
        this.addExp = addExp;
    }

    @Override
    public String toString() {
        return addExp.toString()+"\n<Exp>";
    }
}
