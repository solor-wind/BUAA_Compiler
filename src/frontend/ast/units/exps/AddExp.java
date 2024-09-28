package frontend.ast.units.exps;

import frontend.lexer.Token;

import java.util.Iterator;
import java.util.LinkedList;

public class AddExp{
    //左递归文法改写
    LinkedList<MulExp> mulExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addmulExp(MulExp mulExp) {
        mulExps.add(mulExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<MulExp> it1 = mulExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<AddExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString();
    }
}
