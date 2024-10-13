package frontend.ast.units.exps;

import frontend.lexer.Token;
import frontend.symbols.SymbolTable;

import java.util.Iterator;
import java.util.LinkedList;

public class AddExp {
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

    public boolean checkError(SymbolTable symbolTable) {
        boolean flag = false;
        for (MulExp mulExp : mulExps) {
            flag = flag || mulExp.checkError(symbolTable);
        }
        return flag;
    }

    public int evaluate(SymbolTable symbolTable) {
        int ans = 0;
        for (MulExp mulExp : mulExps) {
            ans += mulExp.evaluate(symbolTable);
        }
        return ans;
    }

    public String getType() {
        return mulExps.getFirst().getType();
    }
}
