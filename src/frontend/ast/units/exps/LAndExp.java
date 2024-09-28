package frontend.ast.units.exps;

import frontend.lexer.Token;

import java.util.Iterator;
import java.util.LinkedList;

public class LAndExp {
    //左递归文法改写
    LinkedList<EqExp> eqExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addEqExp(EqExp eqExp) {
        eqExps.add(eqExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<EqExp> it1 = eqExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<LAndExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString();
    }
}
