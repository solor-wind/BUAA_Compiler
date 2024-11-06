package frontend.ast.units.stmts;

import frontend.lexer.Token;
import frontend.symbols.*;
import ir.IRBuilder;
import ir.instr.GetPtrInstr;
import ir.type.ArrayType;
import ir.type.IntegerType;
import ir.type.PointerType;
import ir.value.*;

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
        symKey = symbolTable.getKeyToIR(ident.getValue());
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
            if (varSym.is("Const")) {
                return varSym.getInitVal();
            }
        }
        System.out.println(ident.getLine() + " " + ident.getValue() + "LVal在evaluate时出错\n");
        return 0;
    }

    public String getType() {
        return type;
    }

    private String symKey;

    public Variable genIR(Function function, BasicBlock basicBlock) {
        Variable var = (Variable) function.getVariable(symKey);
        if (lbrack == null) {
            if (var.isGlobal()) {
                if (!var.isArray()) {
                    var = new Variable(var.getName(), new PointerType(var.getType()));
                } else {
                    Variable res = new Variable(IRBuilder.getVarName(), new PointerType(((ArrayType) var.getType()).getElementType()));
                    basicBlock.addInstruction(new GetPtrInstr(res, var, new Literal(0, new IntegerType(32))));
                    var = res;
                    var.setArray(true);
                }
            }
            return var;
        }
        Value offset = exp.genIR(function, basicBlock);
        //TODO:value为0时，优化？
        Variable res;
        if (var.getType() instanceof PointerType) {
            res = new Variable(IRBuilder.getVarName(), var.getType());
        } else {
            res = new Variable(IRBuilder.getVarName(), new PointerType(((ArrayType) var.getType()).getElementType()));
        }
        basicBlock.addInstruction(new GetPtrInstr(res, var, offset));
        return res;
    }
}
