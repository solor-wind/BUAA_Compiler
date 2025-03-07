package frontend.ast.units.stmts;

import frontend.lexer.Token;
import frontend.symbols.SymbolTable;
import ir.IRBuilder;
import ir.instr.LoadInstr;
import ir.type.IntegerType;
import ir.type.PointerType;
import ir.value.*;

public class PrimaryExp {
    private Token lparent;
    private Exp exp = null;
    private Token rparent;

    private LVal lVal = null;
    private IntConst intConst = null;//+Number
    private CharConst charConst = null;//+Character

    public PrimaryExp() {
    }

    public PrimaryExp(LVal lVal) {
        this.lVal = lVal;
    }

    public PrimaryExp(IntConst intConst) {
        this.intConst = intConst;
    }

    public PrimaryExp(CharConst charConst) {
        this.charConst = charConst;
    }

    public void setLparent(Token lparent) {
        this.lparent = lparent;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void setRparent(Token rparent) {
        this.rparent = rparent;
    }

    @Override
    public String toString() {
        if (exp != null) {
            return lparent + "\n" + exp + "\n" + rparent + "\n<PrimaryExp>";
        } else if (lVal != null) {
            return lVal + "\n<PrimaryExp>";
        } else if (intConst != null) {
            return intConst + "\n<Number>\n<PrimaryExp>";
        } else {
            return charConst + "\n<Character>\n<PrimaryExp>";
        }
    }

    public boolean checkError(SymbolTable symbolTable) {
        if (lVal != null) {
            return lVal.checkError(symbolTable);
        } else if (exp != null) {
            return exp.checkError(symbolTable);
        }
        return false;
    }

    public int evaluate(SymbolTable symbolTable) {
        if (intConst != null) {
            return Integer.parseInt(intConst.token.getValue());
        } else if (charConst != null) {
            return charConst.token.getValue().charAt(0);
        } else if (lVal != null) {
            return lVal.evaluate(symbolTable);
        } else {
            return exp.evaluate(symbolTable);
        }
    }

    public String getType() {
        if (lVal != null) {
            return lVal.getType();
        } else if (exp != null) {
            return exp.getType();
        }
        return "Int";
    }

    public Value genIR(Function function, BasicBlock basicBlock) {
        if (intConst != null) {
            return new Literal(Integer.parseInt(intConst.getToken().getValue()), new IntegerType(32));
        } else if (charConst != null) {
            return new Literal(charConst.getToken().getValue().charAt(0), new IntegerType(32));
        } else if (lparent != null) {
            return exp.genIR(function, basicBlock);
        } else {
            Variable var = lVal.genIR(function, basicBlock);
            if (var.isArray()) {
                return var;
            }
            if (var.isConstant()) {
                if(var.getType() instanceof PointerType ptr){
                    return new Literal((int) var.getInitValue(), ptr.getBaseType());
                }
                return new Literal((int) var.getInitValue(), var.getType());
            }
            Variable res = new Variable(IRBuilder.getVarName(), ((PointerType) var.getType()).getBaseType());
            basicBlock.addInstruction(new LoadInstr(res, var));
            res = (Variable) IRBuilder.changeType(basicBlock, res, new IntegerType(32));
            return res;
        }
    }
}
