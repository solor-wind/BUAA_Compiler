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
import ir.instr.AllocaInstr;
import ir.instr.StoreInstr;
import ir.instr.GetPtrInstr;
import ir.type.ArrayType;
import ir.type.IntegerType;
import ir.type.PointerType;
import ir.type.Type;
import ir.value.Function;
import ir.value.Literal;
import ir.value.Value;
import ir.value.Variable;

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
        this.symbolTable = symbolTable;
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
                    if (initVal.getStringConst() != null) {
                        char[] s = initVal.getStringConst().toCharArray();
                        LinkedList<Object> list = new LinkedList<>();
                        for (char c : s) {
                            list.add((int) c);
                        }
                        arraySym.setInitVals(list);
                    } else {
                        LinkedList<Object> list = new LinkedList<>(initVal.getExps());
                        arraySym.setInitVals(list);
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
                    varSym.setInitVal(initVal.getExps().getFirst());
                }
            }

            if (!isError) {
                symbolTable.addSymbol(varSym);
            }
        }
    }

    private SymbolTable symbolTable;

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
            if (initVal != null) {
                variable.setInitValue(initVal.getExps().getFirst().evaluate(symbolTable));
            } else {
                variable.setInitValue(0);
            }
            return variable;
        }
        ArraySym arraySym = (ArraySym) symbolTable.getSymbol(ident.getValue());
        type = new ArrayType(type, (arraySym).getLength());
        variable = new Variable("@" + ident.getValue(), type,false,true);
        IRBuilder.irModule.addGlobalVariable(ident.getValue(), variable);
        ArrayList<Integer> initVals = new ArrayList<>();
        //TODO:字符串初始化、只初始化一部分?
        if(arraySym.getInitVals() != null) {
            for (Object o : arraySym.getInitVals()) {
                if (o instanceof Exp exp) {
                    initVals.add(exp.evaluate(symbolTable));
                } else {
                    initVals.add((int) o);
                }
            }
        }
        variable.setInitValue(initVals);
        return variable;
    }

    public void genIR(Function function, Token BType) {
        Type type = BType.is(TokenType.INTTK) ? new IntegerType(32) : new IntegerType(8);
        Variable variable;
        if (lbrack == null) {
            //int a;
            //int a=0;
            //char s='1';
            variable = new Variable(IRBuilder.getVarName(), new PointerType(type));
            VarSym varSym = (VarSym) symbolTable.getSymbol(ident.getValue());
            function.addVariable(symbolTable.getKeyToIR(ident.getValue()), variable);
            IRBuilder.currentBlock.addInstruction(new AllocaInstr(variable));
            if (initVal != null) {
                //variable.setInitValue(varSym.getInitVal());
                IRBuilder.currentBlock.addInstruction(new StoreInstr(initVal.genIR(function).get(0), variable));
            }
            return;
        }

        //TODO:将未初始化的部分赋值为0
        ArraySym arraySym = (ArraySym) symbolTable.getSymbol(ident.getValue());
        variable = new Variable(IRBuilder.getVarName(), new PointerType(type));
        IRBuilder.currentBlock.addInstruction(new AllocaInstr(variable, arraySym.getLength()));
        function.addVariable(symbolTable.getKeyToIR(ident.getValue()), variable);

        if (initVal != null) {
//            ArrayList<Integer> initVals = new ArrayList<>(arraySym.getInitVals().size());
//            for (Object o : arraySym.getInitVals()) {
//                if (o instanceof Exp exp) {
//                    initVals.add(exp.evaluate(symbolTable));
//                } else {
//                    initVals.add((int) o);
//                }
//            }
//            variable.setInitValue(initVals);
            ArrayList<Value> initVals = initVal.genIR(function);
            for (int i = 0; i < initVals.size(); i++) {
                Variable res = new Variable(IRBuilder.getVarName(), new PointerType(type));
                IRBuilder.currentBlock.addInstruction(new GetPtrInstr(res, variable, new Literal(i, new IntegerType(32))));
                IRBuilder.currentBlock.addInstruction(new StoreInstr(initVals.get(i), res));
            }
        }
    }
}
