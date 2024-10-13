package frontend.ast.units.exps;

import frontend.ast.units.defs.FuncRParams;
import frontend.ast.units.stmts.Exp;
import frontend.ast.units.stmts.PrimaryExp;
import frontend.lexer.Token;
import frontend.symbols.FuncSym;
import frontend.symbols.GetSymTable;
import frontend.symbols.Symbol;
import frontend.symbols.SymbolTable;

import java.util.Iterator;
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

    public boolean checkError(SymbolTable symbolTable) {
        boolean flag = false;
        if (ident != null) {
            //error c
            Symbol symbol = symbolTable.getSymbol(ident.getValue());
            if (!(symbol instanceof FuncSym)) {
                GetSymTable.addError(ident.getLine(), "c");
                return true;
            }
            //error d
            if (funcRParams == null && !((FuncSym) symbol).getArgs().isEmpty()) {
                GetSymTable.addError(ident.getLine(), "d");
                return true;
            }
            if (funcRParams != null) {
                FuncSym funcSym = (FuncSym) symbol;
                if (funcRParams.getExps().size() != funcSym.getArgs().size()) {
                    GetSymTable.addError(ident.getLine(), "d");
                    return true;
                }
                //error e,函数参数类型匹配
                Iterator<Symbol> params = funcSym.getArgs().iterator();
                Iterator<Exp> it = funcRParams.getExps().iterator();
                while (it.hasNext() && params.hasNext()) {
                    Exp exp = it.next();
                    Symbol param = params.next();
                    if (exp.checkError(symbolTable)) {
                        flag = true;
                        continue;
                    }
                    String type = exp.getType();
                    if ((param.is("Array") && !type.contains("Array")) ||
                            (!param.is("Array") && type.contains("Array")) ||
                            (param.is("CharArray") && type.contains("IntArray")) ||
                            (param.is("IntArray") && type.contains("CharArray"))) {
                        GetSymTable.addError(ident.getLine(), "e");
                    }
                }
            }
            return flag;
        }
        return primaryExp.checkError(symbolTable);
    }

    /**
     * 仅仅适用于常量表达式
     */
    public int evaluate(SymbolTable symbolTable) {
        return primaryExp.evaluate(symbolTable);
    }

    public String getType() {
        if (ident != null) {
            return "Int";
        } else {
            return primaryExp.getType();
        }
    }
}
