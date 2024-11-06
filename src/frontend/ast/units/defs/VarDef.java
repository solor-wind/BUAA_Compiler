package frontend.ast.units.defs;

import frontend.ast.units.exps.ConstExp;
import frontend.ast.units.stmts.Exp;
import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.symbols.ArraySym;
import frontend.symbols.GetSymTable;
import frontend.symbols.SymbolTable;
import frontend.symbols.VarSym;
import ir.IRBuilder;
import ir.instr.*;
import ir.type.ArrayType;
import ir.type.IntegerType;
import ir.type.PointerType;
import ir.type.Type;
import ir.value.*;

import java.util.ArrayList;
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
                    if (symbolTable.getOutID() == 1) {
                        //仅全局变量赋予初值
                        if (initVal.getStringConst() != null) {
                            char[] s = initVal.getStringConst().toCharArray();
                            LinkedList<Integer> list = new LinkedList<>();
                            for (char c : s) {
                                list.add((int) c);
                            }
                            arraySym.setInitVals(list);
                        } else {
                            LinkedList<Integer> list = new LinkedList<>();
                            for (Exp exp : initVal.getExps()) {
                                list.add(exp.evaluate(symbolTable));
                            }
                            arraySym.setInitVals(list);
                        }
                    }
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
                    if (symbolTable.getOutID() == 1) {
                        varSym.setInitVal(initVal.getExps().getFirst().evaluate(symbolTable));
                    }
                }
            }

            if (!isError) {
                symbolTable.addSymbol(varSym);
            }
        }
        symKey = symbolTable.getKeyToIR(ident.getValue());
    }

    private String symKey;

    public Variable genGlobalIR(Token BType) {
        //全局变量要么有给定的初值，要么就默认为0
        Type type = BType.is(TokenType.INTTK) ? new IntegerType(32) : new IntegerType(8);
        Variable variable;
        if (lbrack == null) {
            //int a;
            //int a=0;
            //char s='1';
            variable = new Variable("@" + ident.getValue(), type, false, true);
            IRBuilder.irModule.addGlobalVariable(ident.getValue(), variable);
            VarSym varSym = (VarSym) GetSymTable.symMap.get(symKey);
            variable.setInitValue(varSym.getInitVal());
            return variable;
        }
        ArraySym arraySym = (ArraySym) GetSymTable.symMap.get(symKey);
        type = new ArrayType(type, (arraySym).getLength());
        variable = new Variable("@" + ident.getValue(), type, false, true);
        variable.setArray(true);
        IRBuilder.irModule.addGlobalVariable(ident.getValue(), variable);
        ArrayList<Integer> initVals = new ArrayList<>(arraySym.getInitVals());
        //TODO:字符串初始化、只初始化一部分?
        variable.setInitValue(initVals);
        return variable;
    }

    public void genIR(Function function, BasicBlock basicBlock, Token BType) {
        Type type = BType.is(TokenType.INTTK) ? new IntegerType(32) : new IntegerType(8);
        Variable variable;
        if (lbrack == null) {
            //int a;
            //int a=0;
            //char s='1';
            variable = new Variable(IRBuilder.getVarName(), new PointerType(type));
            //VarSym varSym = (VarSym) symbolTable.getSymbol(ident.getValue());
            function.addVariable(symKey, variable);
            basicBlock.addInstruction(new AllocaInstr(variable));
            if (initVal != null) {
                //variable.setInitValue(varSym.getInitVal());
                Value initValue = initVal.genIR(function, basicBlock).get(0);
                initValue = IRBuilder.changeType(basicBlock, initValue, type);//隐式类型转换
                basicBlock.addInstruction(new StoreInstr(initValue, variable));
            }
            return;
        }

        //TODO:将未初始化的部分赋值为0
        ArraySym arraySym = (ArraySym) GetSymTable.symMap.get(symKey);
        variable = new Variable(IRBuilder.getVarName(), new PointerType(type));
        variable.setArray(true);
        basicBlock.addInstruction(new AllocaInstr(variable, arraySym.getLength()));
        function.addVariable(symKey, variable);

        if (initVal != null) {
            ArrayList<Value> initVals = initVal.genIR(function, basicBlock);
            for (int i = 0; i < initVals.size(); i++) {
                Variable res = new Variable(IRBuilder.getVarName(), new PointerType(type));
                basicBlock.addInstruction(new GetPtrInstr(res, variable, new Literal(i, new IntegerType(32))));
                basicBlock.addInstruction(new StoreInstr(IRBuilder.changeType(basicBlock, initVals.get(i), type), res));
            }
        }
    }
}
