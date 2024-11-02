package frontend.ast.units.defs;

import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.symbols.*;
import ir.IRBuilder;
import ir.type.IntegerType;
import ir.type.PointerType;
import ir.type.Type;
import ir.value.Argument;
import ir.value.Function;

public class FuncFParam {
    private Token bType;
    private Token ident;
    private Token lbrack = null;
    private Token rbrack;

    public FuncFParam(Token bType, Token ident) {
        this.bType = bType;
        this.ident = ident;
    }

    public void setLbrack(Token lbrack) {
        this.lbrack = lbrack;
    }

    public void setRbrack(Token rbrack) {
        this.rbrack = rbrack;
    }

    @Override
    public String toString() {
        if (lbrack == null) {
            return bType + "\n" + ident + "\n<FuncFParam>";
        } else {
            return bType + "\n" + ident + "\n" + lbrack + "\n" + rbrack + "\n<FuncFParam>";
        }
    }

    public void checkError(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        //error b
        if (symbolTable.hasDefined(ident.getValue())) {
            GetSymTable.addError(ident.getLine(), "b");
        } else {
            Symbol symbol;
            String type = bType.is(TokenType.INTTK) ? "Int" : "Char";
            if (lbrack != null) {
                symbol = new ArraySym(ident.getValue(), type + "Array");
            } else {
                symbol = new VarSym(ident.getValue(), type);
            }
            symbolTable.addSymbol(symbol);
            symbolTable.getFuncSym().addArg(symbol);
        }
    }

    private SymbolTable symbolTable;

    public Argument genIR(Function function) {
        Type type = bType.is(TokenType.INTTK) ? new IntegerType(32) : new IntegerType(8);
        if (lbrack != null) {
            //数组转指针
            type = new PointerType(type);
        }
        String key = symbolTable.getKeyToIR(ident.getValue());
        Argument argument = new Argument(IRBuilder.getVarName(), type);
        function.addVariable(key, argument);
        return argument;
    }
}
