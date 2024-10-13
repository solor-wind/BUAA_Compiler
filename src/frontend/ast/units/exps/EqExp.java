package frontend.ast.units.exps;

import frontend.lexer.Token;
import frontend.symbols.SymbolTable;

import java.util.Iterator;
import java.util.LinkedList;

public class EqExp {
    //左递归文法改写
    LinkedList<RelExp> relExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addRelExp(RelExp relExp) {
        relExps.add(relExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<RelExp> it1 = relExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<EqExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString();
    }

    public void checkError(SymbolTable symbolTable) {
        for (RelExp relExp : relExps) {
            relExp.checkError(symbolTable);
        }
    }
}
