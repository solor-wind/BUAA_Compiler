package frontend.ast.units.stmts;

import frontend.lexer.Token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class Stmt implements BlockItem {
    private Token keyword;//if,for,break,continue,return,getint,getchar,printf
    private ArrayList<Token> idents = new ArrayList<>();

    private LVal lVal = null;
    private LinkedList<Exp> exps = new LinkedList<>();
    private Block block = null;

    private Cond cond;
    private Stmt stmt1;
    private Stmt stmt2;

    private ForStmt forStmt1;
    private ForStmt forStmt2;

    private Token stringConst;

    public Stmt(Token keyword) {
        this.keyword = keyword;
        idents = new ArrayList<>();
        exps = new LinkedList<>();
    }

    public void setKeyword(Token keyword) {
        this.keyword = keyword;
    }

    public void setLVal(LVal lVal) {
        this.lVal = lVal;
    }

    public void addIdent(Token ident) {
        idents.add(ident);
    }

    public void addExp(Exp exp) {
        exps.add(exp);
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setCond(Cond cond) {
        this.cond = cond;
    }

    public void setStmt1(Stmt stmt1) {
        this.stmt1 = stmt1;
    }

    public void setStmt2(Stmt stmt2) {
        this.stmt2 = stmt2;
    }

    public void setForStmt1(ForStmt forStmt1) {
        this.forStmt1 = forStmt1;
    }

    public void setForStmt2(ForStmt forStmt2) {
        this.forStmt2 = forStmt2;
    }

    public void setStringConst(Token stringConst) {
        this.stringConst = stringConst;
    }

    @Override
    public String toString() {
        String string = "<Stmt>";
        if (keyword == null) {
            if (lVal != null) {
                return lVal.toString() + "\n" + idents.get(0) + "\n"
                        + exps.getFirst() + "\n" + idents.get(1) + "\n" + string;
            } else if (block != null) {
                return block + "\n" + string;
            } else {
                if (exps.isEmpty()) {
                    return idents.get(0) + "\n" + string;
                } else {
                    return exps.getFirst() + "\n" + idents.get(0) + "\n" + string;
                }
            }
        }
        String ret = "";
        switch (keyword.getToken()) {
            case IFTK:
                ret = keyword + "\n" + idents.get(0) + "\n" + cond + "\n" +
                        idents.get(1) + "\n" + stmt1 + "\n";
                if (idents.size() > 2) {
                    ret += idents.get(2) + "\n" + stmt2 + "\n";
                }
                break;
            case FORTK:
                ret = keyword + "\n" + idents.get(0) + "\n";
                if (forStmt1 != null) {
                    ret += forStmt1.toString() + "\n";

                }
                ret += idents.get(1) + "\n";
                if (cond != null) {
                    ret += cond.toString() + "\n";
                }
                ret += idents.get(2) + "\n";
                if (forStmt2 != null) {
                    ret += forStmt2.toString() + "\n";
                }
                ret += idents.get(3) + "\n" + stmt1 + "\n";
                break;
            case BREAKTK, CONTINUETK:
                ret = keyword + "\n" + idents.get(0) + "\n";
                break;
            case RETURNTK:
                if (exps.isEmpty()) {
                    ret = keyword + "\n" + idents.get(0) + "\n";
                } else {
                    ret = keyword + "\n" + exps.getFirst() + "\n" + idents.get(0) + "\n";
                }
                break;
            case GETINTTK, GETCHARTK:
                ret = lVal + "\n" + idents.get(0) + "\n" + keyword + "\n" + idents.get(1) + "\n" + idents.get(2) + "\n" + idents.get(3) + "\n";
                break;
            default: {
                StringBuilder sb = new StringBuilder();
                sb.append(keyword + "\n" + idents.get(0) + "\n" + stringConst + "\n");
                int i = 1;
                Iterator<Exp> it = exps.iterator();
                while (it.hasNext()) {
                    sb.append(idents.get(i) + "\n" + it.next() + "\n");
                    i++;
                }
                ret += sb.toString() + idents.get(i) + "\n" + idents.get(i + 1) + "\n";
            }
        }
        return ret + string;
    }
}
