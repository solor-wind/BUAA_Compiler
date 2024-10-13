package frontend.ast.units.defs;

import frontend.ast.units.stmts.Exp;
import frontend.lexer.Token;
import frontend.symbols.SymbolTable;

import java.util.Iterator;
import java.util.LinkedList;

public class InitVal {
    private LinkedList<Exp> exps = new LinkedList<>();
    private LinkedList<Token> commas = new LinkedList<>();
    private Token lbrace;
    private Token rbrace;
    private Token stringConst = null;

    public void addExp(Exp exp) {
        exps.add(exp);
    }

    public LinkedList<Exp> getExps() {
        return exps;
    }

    public void addComma(Token comma) {
        commas.add(comma);
    }

    public void setLbrace(Token lbrace) {
        this.lbrace = lbrace;
    }

    public void setRbrace(Token rbrace) {
        this.rbrace = rbrace;
    }

    public void setStringConst(Token stringConst) {
        this.stringConst = stringConst;
    }

    @Override
    public String toString() {
        if (stringConst != null) {
            return stringConst + "\n<InitVal>";
        } else if (lbrace == null) {
            return exps.getFirst() + "\n<InitVal>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(lbrace + "\n");
        Iterator<Exp> it = exps.iterator();
        Iterator<Token> it2 = commas.iterator();
        while (it.hasNext()) {
            sb.append(it.next() + "\n");
            if (it2.hasNext()) {
                sb.append(it2.next() + "\n");
            }
        }
        sb.append(rbrace + "\n");
        return sb.toString() + "<InitVal>";
    }

    public boolean checkError(SymbolTable symbolTable) {
        boolean flag = false;
        if (stringConst != null) {
            return flag;
        } else {
            for (Exp exp : exps) {
                flag = flag || exp.checkError(symbolTable);
            }
        }
        return flag;
    }
}
