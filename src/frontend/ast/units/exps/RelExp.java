package frontend.ast.units.exps;

import frontend.lexer.Token;

import java.util.Iterator;
import java.util.LinkedList;

public class RelExp {
    //左递归文法改写
    LinkedList<AddExp> addExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addAddExp(AddExp addExp) {
        addExps.add(addExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<AddExp> it1 = addExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<RelExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString();
    }
}
