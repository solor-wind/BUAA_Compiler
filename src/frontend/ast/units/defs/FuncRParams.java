package frontend.ast.units.defs;

import frontend.ast.units.stmts.Exp;
import frontend.lexer.Token;

import java.util.Iterator;
import java.util.LinkedList;

public class FuncRParams {
    private LinkedList<Exp> exps = new LinkedList<>();
    private LinkedList<Token> commas = new LinkedList<>();

    public void addExp(Exp exp) {
        exps.add(exp);
    }

    public void addComma(Token comma) {
        commas.add(comma);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Exp> it = exps.iterator();
        Iterator<Token> it2 = commas.iterator();
        while (it.hasNext()) {
            sb.append(it.next() + "\n");
            if (it2.hasNext()) {
                sb.append(it2.next() + "\n");
            }
        }
        return sb + "<FuncRParams>";
    }
}
