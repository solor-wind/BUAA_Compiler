package frontend.ast.units.defs;

import frontend.ast.units.stmts.Exp;
import frontend.lexer.Token;
import ir.value.*;

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

    public ArrayList<Value> genIR(Function function, BasicBlock basicBlock) {
        ArrayList<Value> args = new ArrayList<>();
        for (Exp exp : exps) {
            args.add(exp.genIR(function, basicBlock));
        }
        return args;
    }
}
