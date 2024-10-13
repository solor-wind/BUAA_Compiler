package frontend.ast.units.exps;

import frontend.lexer.Token;
import frontend.symbols.SymbolTable;

import java.util.Iterator;
import java.util.LinkedList;

public class MulExp {
    //左递归文法改写
    LinkedList<UnaryExp> unaryExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addUnaryExp(UnaryExp unaryExp) {
        unaryExps.add(unaryExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<UnaryExp> it1 = unaryExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<MulExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString();
    }

    public boolean checkError(SymbolTable symbolTable) {
        boolean flag = false;
        for (UnaryExp unaryExp : unaryExps) {
            flag = flag || unaryExp.checkError(symbolTable);
        }
        return flag;
    }

    public int evaluate(SymbolTable symbolTable) {
        int ans = 1;
        for (UnaryExp unaryExp : unaryExps) {
            ans *= unaryExp.evaluate(symbolTable);
        }
        return ans;
    }

    public String getType() {
        return unaryExps.getFirst().getType();
    }
}
