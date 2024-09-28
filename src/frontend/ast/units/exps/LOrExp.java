package frontend.ast.units.exps;

import frontend.ast.units.stmts.Cond;
import frontend.lexer.Token;

import java.util.Iterator;
import java.util.LinkedList;

public class LOrExp extends Cond {
    //左递归文法改写
    LinkedList<LAndExp> lAndExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addLAndExp(LAndExp lAndExp) {
        lAndExps.add(lAndExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<LAndExp> it1 = lAndExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<LOrExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString()+"\n<Cond>";
    }
}
