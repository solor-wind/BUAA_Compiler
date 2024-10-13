package frontend.ast.units.defs;

import frontend.lexer.Token;

import java.util.Iterator;
import java.util.LinkedList;

public class FuncFParams {
    private LinkedList<FuncFParam> funcFParams = new LinkedList<>();
    private LinkedList<Token> commas = new LinkedList<>();

    public void addFuncFParam(FuncFParam funcFParam) {
        funcFParams.add(funcFParam);
    }

    public void addComma(Token comma) {
        commas.add(comma);
    }

    public LinkedList<FuncFParam> getFuncFParams() {
        return funcFParams;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<FuncFParam> it = funcFParams.iterator();
        Iterator<Token> it2 = commas.iterator();
        while (it.hasNext()) {
            sb.append(it.next() + "\n");
            if (it2.hasNext()) {
                sb.append(it2.next() + "\n");
            }
        }
        return sb + "<FuncFParams>";
    }
}
