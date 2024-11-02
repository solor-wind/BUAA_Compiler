package frontend.ast.units.defs;

import frontend.ast.units.stmts.Exp;
import frontend.lexer.Token;
import ir.value.Argument;
import ir.value.Function;

import java.util.ArrayList;
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

    public LinkedList<Exp> getExps() {
        return exps;
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

    public ArrayList<Argument> genIR(Function function) {
        ArrayList<Argument> args = new ArrayList<>();
        for (Exp exp : exps) {
            args.add(new Argument(exp.genIR(function)));
        }
        return args;
    }
}
