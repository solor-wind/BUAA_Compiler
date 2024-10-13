package frontend.ast.units.defs;

import frontend.ast.units.exps.ConstExp;
import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.symbols.ArraySym;
import frontend.symbols.GetSymTable;
import frontend.symbols.SymbolTable;
import frontend.symbols.VarSym;

import java.util.LinkedList;

public class VarDef {
    private Token ident;
    private Token lbrack;
    private Token rbrack;
    private ConstExp constExp;
    private Token assign;
    private InitVal initVal;

    public VarDef(Token ident) {
        this.ident = ident;
    }

    public void setLbrack(Token lbrack) {
        this.lbrack = lbrack;
    }

    public void setRbrack(Token rbrack) {
        this.rbrack = rbrack;
    }

    public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }

    public void setAssign(Token assign) {
        this.assign = assign;
    }

    public void setInitVal(InitVal initVal) {
        this.initVal = initVal;
    }

    public Token getLbrack() {
        return lbrack;
    }

    public Token getIdent() {
        return ident;
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    public InitVal getInitVal() {
        return initVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident + "\n");
        if (lbrack != null) {
            sb.append(lbrack + "\n");
            sb.append(constExp + "\n");
            sb.append(rbrack + "\n");
        }
        if (assign != null) {
            sb.append(assign + "\n");
            sb.append(initVal + "\n");
        }
        return sb.toString() + "<VarDef>";
    }

    public void checkError(SymbolTable symbolTable, Token BType) {
        boolean isError = false;
        //error b
        if (symbolTable.hasDefined(ident.getValue())) {
            GetSymTable.addError(ident.getLine(), "b");
            isError = true;
        }

        if (lbrack != null) {
            String type = BType.is(TokenType.INTTK) ? "IntArray" : "CharArray";
            ArraySym arraySym = new ArraySym(ident.getValue(), type);
            if (!constExp.checkError(symbolTable)) {
                arraySym.setLength(constExp.evaluate(symbolTable));
            }
            //初值
            if (initVal != null) {
                if (!initVal.checkError(symbolTable)) {
                    /*TODO:暂时不做evaluate*/
                    LinkedList<Object> list = new LinkedList<>(initVal.getExps());
                    arraySym.setInitVals(list);
                }
            }

            if (!isError) {
                symbolTable.addSymbol(arraySym);
            }
        } else {
            String type = BType.is(TokenType.INTTK) ? "Int" : "Char";
            VarSym varSym = new VarSym(ident.getValue(), type);
            if (initVal != null && !initVal.getExps().isEmpty()) {
                if (!initVal.checkError(symbolTable)) {
                    varSym.setInitVal(initVal.getExps().getFirst());
                }
            }

            if (!isError) {
                symbolTable.addSymbol(varSym);
            }
        }
    }
}
