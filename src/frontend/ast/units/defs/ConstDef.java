package frontend.ast.units.defs;

import frontend.lexer.Token;
import frontend.ast.units.exps.ConstExp;
import frontend.lexer.TokenType;
import frontend.symbols.ArraySym;
import frontend.symbols.GetSymTable;
import frontend.symbols.SymbolTable;
import frontend.symbols.VarSym;

public class ConstDef {
    private Token ident;//常量名

    private Token lbrack = null;//[
    private ConstExp constExp;
    private Token rbrack = null;//]

    private Token assign;//=
    private ConstInitVal constInitVal;

    public ConstDef(Token ident) {
        this.ident = ident;
    }

    public void setLbrack(Token lbrack) {
        this.lbrack = lbrack;
    }

    public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }

    public void setRbrack(Token rbrack) {
        this.rbrack = rbrack;
    }

    public void setAssign(Token assign) {
        this.assign = assign;
    }

    public void setConstInitVal(ConstInitVal constInitVal) {
        this.constInitVal = constInitVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident + "\n");
        if (lbrack != null) {
            sb.append(lbrack + "\n").append(constExp + "\n").append(rbrack + "\n");
        }
        sb.append(assign + "\n").append(constInitVal + "\n");
        return sb.toString() + "<ConstDef>";
    }

    public void checkError(SymbolTable symbolTable, Token BType) {
        boolean isError = false;
        //error b
        if (symbolTable.hasDefined(ident.getValue())) {
            GetSymTable.addError(ident.getLine(), "b");
            isError = true;
        }

        if (lbrack != null) {
            String type = BType.is(TokenType.INTTK) ? "ConstIntArray" : "ConstCharArray";
            ArraySym arraySym = new ArraySym(ident.getValue(), type);
            if (!constExp.checkError(symbolTable)) {
                arraySym.setLength(constExp.evaluate(symbolTable));
            }
            //初值
            if (!constInitVal.checkError(symbolTable)) {
                arraySym.setInitVals(constInitVal.evaluateArray(symbolTable));
            }
            if (!isError) {
                symbolTable.addSymbol(arraySym);
            }
        } else {
            String type = BType.is(TokenType.INTTK) ? "ConstInt" : "ConstChar";
            VarSym varSym = new VarSym(ident.getValue(), type);
            if (!constInitVal.checkError(symbolTable)) {
                varSym.setInitVal(constInitVal.evaluateVar(symbolTable));
            }
            if (!isError) {
                symbolTable.addSymbol(varSym);
            }
        }
    }
}
