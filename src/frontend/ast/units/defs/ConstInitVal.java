package frontend.ast.units.defs;

import frontend.ast.units.exps.ConstExp;
import frontend.lexer.Token;
import frontend.symbols.SymbolTable;

import java.util.Iterator;
import java.util.LinkedList;

public class ConstInitVal {
    private LinkedList<ConstExp> constExps;
    private Token lbrace = null;
    private LinkedList<Token> commas;
    private Token rbrace = null;
    private Token stringConst = null;

    public ConstInitVal() {
        constExps = new LinkedList<>();
        commas = new LinkedList<>();
    }

    public void addConstExp(ConstExp constExp) {
        constExps.add(constExp);
    }

    public void setLbrace(Token lbrace) {
        this.lbrace = lbrace;
    }

    public void addCommas(Token commas) {
        this.commas.add(commas);
    }

    public void setRbrace(Token rbrace) {
        this.rbrace = rbrace;
    }

    public void setStringConst(Token stringConst) {
        this.stringConst = stringConst;
    }

    public String getStringConst() {
        if (stringConst != null) {
            return stringConst.getValue();
        }
        return null;
    }

    @Override
    public String toString() {
        if (stringConst != null) {
            return stringConst + "\n<ConstInitVal>";
        } else if (lbrace == null) {
            return constExps.getFirst() + "\n<ConstInitVal>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(lbrace + "\n");
        Iterator<ConstExp> it = constExps.iterator();
        Iterator<Token> it2 = commas.iterator();
        while (it.hasNext()) {
            sb.append(it.next() + "\n");
            if (it2.hasNext()) {
                sb.append(it2.next() + "\n");
            }
        }
        sb.append(rbrace + "\n");
        return sb.toString() + "<ConstInitVal>";
    }

    public boolean checkError(SymbolTable symbolTable) {
        boolean flag = false;
        if (stringConst != null) {
            return flag;
        }
        for (ConstExp constExp : constExps) {
            flag = flag || constExp.checkError(symbolTable);
        }
        return flag;
    }

    public LinkedList<Integer> evaluateArray(SymbolTable symbolTable) {
        LinkedList<Integer> list = new LinkedList<>();
        for (ConstExp constExp : constExps) {
            list.add(constExp.evaluate(symbolTable));
        }
        return list;
    }

    public int evaluateVar(SymbolTable symbolTable) {
        return constExps.getFirst().evaluate(symbolTable);
    }
}
