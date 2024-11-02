package frontend.ast.units.stmts;

import frontend.lexer.Token;
import frontend.symbols.*;
import ir.IRBuilder;
import ir.instr.GetPtrInstr;
import ir.type.ArrayType;
import ir.type.PointerType;
import ir.value.Function;
import ir.value.Value;
import ir.value.Variable;

public class LVal {
    private Token ident;
    private Token lbrack = null;
    private Exp exp;
    private Token rbrack;

    private String type = "Int";

    public LVal(Token ident) {
        this.ident = ident;
    }

    public Token getIdent() {
        return ident;
    }

    public void setLbrack(Token lbrack) {
        this.lbrack = lbrack;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void setRbrack(Token rbrack) {
        this.rbrack = rbrack;
    }

    @Override
    public String toString() {
        if (lbrack == null) {
            return ident.toString() + "\n<LVal>";
        }
        return ident.toString() + "\n" + lbrack.toString() + "\n"
                + exp.toString() + "\n" + rbrack.toString() + "\n<LVal>";
    }

    public boolean checkError(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        boolean flag = false;
        //error c
        Symbol symbol = symbolTable.getSymbol(ident.getValue());
        if (symbol == null) {
            GetSymTable.addError(ident.getLine(), "c");
            flag = true;
        } else {
            if (symbol.is("IntArray")) {
                if (lbrack != null) {
                    type = "Int";
                } else {
                    type = "IntArray";
                }
            } else if (symbol.is("CharArray")) {
                if (lbrack != null) {
                    type = "Char";
                } else {
                    type = "CharArray";
                }
            } else {
                type = "Int";
            }
        }

        if (exp != null) {
            flag = flag || exp.checkError(symbolTable);
        }
        return flag;
    }

    public int evaluate(SymbolTable symbolTable) {
        /*TODO:不存在数组？*/
        Symbol symbol = symbolTable.getSymbol(ident.getValue());
        if (symbol instanceof VarSym varSym) {
            if (varSym.is("ConstChar")) {
                if (varSym.getInitVal() instanceof Integer integer) {
                    return (char) integer.intValue();
                }
                return (Character) varSym.getInitVal();
            } else if (varSym.is("IntChar")) {
                if (varSym.getInitVal() instanceof Character character) {
                    return (int) character;
                }
                return (Integer) varSym.getInitVal();
            }
        }
        System.out.println(ident.getLine() + " " + ident.getValue() + "LVal在evaluate时出错\n");
        return 0;
    }

    public String getType() {
        return type;
    }

    private SymbolTable symbolTable;

    public Variable genIR(Function function) {
        Variable var = (Variable) function.getVariable(symbolTable.getKeyToIR(ident.getValue()));
        if (lbrack == null) {
            if (var.isGlobal()) {
                var = new Variable(var.getName(), new PointerType(var.getType()));
            }
            return var;
        }
        Value offset = exp.genIR(function);
        //TODO:value为0时，优化？
        Variable res = new Variable(IRBuilder.getVarName(), new PointerType(((ArrayType) var.getType()).getElementType()));
        IRBuilder.currentBlock.addInstruction(new GetPtrInstr(res, var, offset));
        return res;
    }
}
