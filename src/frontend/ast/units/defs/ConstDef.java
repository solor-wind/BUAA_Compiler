package frontend.ast.units.defs;

import frontend.ast.units.stmts.Exp;
import frontend.lexer.Token;
import frontend.ast.units.exps.ConstExp;
import frontend.lexer.TokenType;
import frontend.symbols.ArraySym;
import frontend.symbols.GetSymTable;
import frontend.symbols.SymbolTable;
import frontend.symbols.VarSym;
import ir.IRBuilder;
import ir.instr.AllocaInstr;
import ir.instr.StoreInstr;
import ir.instr.GetPtrInstr;
import ir.type.ArrayType;
import ir.type.IntegerType;
import ir.type.PointerType;
import ir.type.Type;
import ir.value.*;

import java.util.ArrayList;
import java.util.LinkedList;

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
        this.symbolTable = symbolTable;
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
                if (constInitVal.getStringConst() == null) {
                    arraySym.setInitVals(constInitVal.evaluateArray(symbolTable));
                } else {
                    char[] s = constInitVal.getStringConst().toCharArray();
                    LinkedList<Object> list = new LinkedList<>();
                    for (char c : s) {
                        list.add(c);
                    }
                    arraySym.setInitVals(list);
                }
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

    SymbolTable symbolTable;

    public Variable genGlobalIR(Token BType) {
        //const一定有初值
        Type type = BType.is(TokenType.INTTK) ? new IntegerType(32) : new IntegerType(8);
        Variable variable;
        if (lbrack == null) {
            //int a;
            //int a=0;
            //char s='1';
            variable = new Variable("@" + ident.getValue(), type, true, true);
            IRBuilder.irModule.addGlobalVariable(ident.getValue(), variable);
            VarSym varSym = (VarSym) symbolTable.getSymbol(ident.getValue());
            variable.setInitValue(varSym.getInitVal());
            return new Variable(ident.getValue(), type);
        }
        ArraySym arraySym = (ArraySym) symbolTable.getSymbol(ident.getValue());
        type = new ArrayType(type, arraySym.getLength());
        variable = new Variable("@" + ident.getValue(), type, true, true);
        IRBuilder.irModule.addGlobalVariable(ident.getValue(), variable);
        ArrayList<Integer> initVals = new ArrayList<>(arraySym.getInitVals().size());
        //TODO:字符串初始化、只初始化一部分?
        for (Object o : arraySym.getInitVals()) {
            if (o instanceof Exp exp) {
                initVals.add(exp.evaluate(symbolTable));
            } else {
                initVals.add((int) o);
            }
        }
        variable.setInitValue(initVals);
        return variable;
    }

    public void genIR(Function function, Token BType) {
        Type type = BType.is(TokenType.INTTK) ? new IntegerType(32) : new IntegerType(8);
        Variable variable;
        //const一定有初值
        if (lbrack == null) {
            //int a;
            //int a=0;
            //char s='1';
            variable = new Variable(IRBuilder.getVarName(), new PointerType(type));
            variable.setConstant(true);
            VarSym varSym = (VarSym) symbolTable.getSymbol(ident.getValue());
            variable.setInitValue(varSym.getInitVal());
            function.addVariable(symbolTable.getKeyToIR(ident.getValue()), variable);
            IRBuilder.currentBlock.addInstruction(new AllocaInstr(variable));
            IRBuilder.currentBlock.addInstruction(new StoreInstr(new Literal((int) (variable.getInitValue()), type), variable));
        }

        //TODO:将未初始化的部分赋值为0
        ArraySym arraySym = (ArraySym) symbolTable.getSymbol(ident.getValue());
        variable = new Variable(IRBuilder.getVarName(), new PointerType(type));
        variable.setConstant(true);
        function.addVariable(symbolTable.getKeyToIR(ident.getValue()), variable);
        ArrayList<Integer> initVals = new ArrayList<>(arraySym.getInitVals().size());
        for (Object o : arraySym.getInitVals()) {
            if (o instanceof Exp exp) {
                initVals.add(exp.evaluate(symbolTable));
            } else {
                initVals.add((int) o);
            }
        }
        variable.setInitValue(initVals);
        IRBuilder.currentBlock.addInstruction(new AllocaInstr(variable, arraySym.getLength()));
        for (int i = 0; i < initVals.size(); i++) {
            Variable res = new Variable(IRBuilder.getVarName(), new PointerType(type));
            IRBuilder.currentBlock.addInstruction(new GetPtrInstr(res, variable, new Literal(i, new IntegerType(32))));
            IRBuilder.currentBlock.addInstruction(new StoreInstr(new Literal(initVals.get(i), type), res));
        }
    }
}
