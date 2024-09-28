package frontend.ast.units.exps;

import frontend.ast.units.defs.FuncRParams;
import frontend.ast.units.stmts.PrimaryExp;
import frontend.lexer.Token;

import java.util.LinkedList;

public class UnaryExp {
    private PrimaryExp primaryExp = null;

    private Token ident = null;
    private Token lparent;
    private FuncRParams funcRParams;
    private Token rparent;

    private LinkedList<UnaryOp> unaryOps = new LinkedList<>();//消除左递归

    public UnaryExp() {
        unaryOps = new LinkedList<>();
    }

    public void setPrimaryExp(PrimaryExp primaryExp) {
        this.primaryExp = primaryExp;
    }

    public void setIdent(Token ident) {
        this.ident = ident;
    }

    public void setLparent(Token lparent) {
        this.lparent = lparent;
    }

    public void setRparent(Token rparent) {
        this.rparent = rparent;
    }

    public void setFuncRParams(FuncRParams funcRParams) {
        this.funcRParams = funcRParams;
    }

    public void addUnaryOp(UnaryOp unaryOp) {
        unaryOps.addLast(unaryOp);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (UnaryOp unaryOp : unaryOps) {
            sb.append(unaryOp + "\n");
        }

        String string = "\n<UnaryExp>".repeat((unaryOps.size() + 1));

        if (lparent != null) {
            String ans = sb.toString() + ident + "\n" + lparent + "\n";
            if (funcRParams != null) {
                ans += funcRParams.toString() + "\n";
            }
            ans += rparent + string;
            return ans;
        } else {
            return sb.toString() + primaryExp + string;
        }
    }
}
