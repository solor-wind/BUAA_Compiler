package frontend.ast.units.defs;

import frontend.ast.units.stmts.BlockItem;
import frontend.lexer.Token;
import java.util.Iterator;
import java.util.LinkedList;

public class ConstDecl implements Decl, BlockItem {
    private Token constToken;//const
    private Token BType;//int,char
    private LinkedList<ConstDef> constDefs;
    private LinkedList<Token> commas;//,
    private Token semicn;//;

    public ConstDecl(Token constToken, Token BType) {
        this.constToken = constToken;
        this.BType = BType;
        constDefs = new LinkedList<>();
        commas = new LinkedList<>();
    }

    public void addConstDef(ConstDef constDef) {
        constDefs.add(constDef);
    }

    public void addCommas(Token comma) {
        commas.add(comma);
    }

    public void setSemicn(Token semicn) {
        this.semicn = semicn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(constToken + "\n").append(BType + "\n");
        Iterator<ConstDef> it = constDefs.iterator();
        Iterator<Token> it2 = commas.iterator();
        while (it.hasNext()) {
            sb.append(it.next() + "\n");
            if (it2.hasNext()) {
                sb.append(it2.next() + "\n");
            }
        }
        sb.append(semicn + "\n");
        return sb.toString() + "<ConstDecl>";
    }
}
