package frontend.ast.units.defs;

import frontend.ast.units.stmts.Block;
import frontend.ast.units.stmts.BlockItem;
import frontend.ast.units.stmts.Stmt;
import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.symbols.FuncSym;
import frontend.symbols.GetSymTable;
import frontend.symbols.SymbolTable;
import ir.IRBuilder;
import ir.instr.AllocaInstr;
import ir.instr.StoreInstr;
import ir.type.*;
import ir.value.*;

public class FuncDef implements Unit {
    private Token funcType;
    private Token funcName;
    private Token lparent;
    private Token rparent;
    private FuncFParams funcFParams = null;
    private Block block;

    public FuncDef(Token funcType, Token funcName) {
        this.funcType = funcType;
        this.funcName = funcName;
    }

    public void setLparent(Token lparent) {
        this.lparent = lparent;
    }

    public void setRparent(Token rparent) {
        this.rparent = rparent;
    }

    public void setFuncFParams(FuncFParams funcFParams) {
        this.funcFParams = funcFParams;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public String toString() {
        String para = "";
        if (funcFParams != null) {
            para = funcFParams.toString() + "\n";
        }
        String tmp = funcType + "\n";
        if (!funcName.getValue().equals("main")) {
            tmp += "<FuncType>\n";
        }
        return tmp +
                funcName + "\n" +
                lparent + "\n" +
                para +
                rparent + "\n" +
                block;
    }

    public void checkError(SymbolTable symbolTable) {
        String type;
        if (funcType.is(TokenType.INTTK)) {
            type = "IntFunc";
        } else if (funcType.is(TokenType.CHARTK)) {
            type = "CharFunc";
        } else {
            type = "VoidFunc";
        }
        SymbolTable s = new SymbolTable(symbolTable, new FuncSym(funcName.getValue(), type));
        s.setOutID(++GetSymTable.outID);

        //error b,函数名重名
        if (symbolTable.hasDefined(funcName.getValue())) {
            GetSymTable.addError(funcName.getLine(), "b");
        } else {
            symbolTable.addSymbol(s.getFuncSym());
        }
        symbolTable.addChild(s);

        //error g,缺少返回语句
        boolean isG = !funcType.is(TokenType.VOIDTK);
        if (isG) {
            BlockItem blockItem;
            if (block == null) {
                blockItem = null;
            } else if (block.getBlockItems().isEmpty()) {
                blockItem = null;
            } else {
                blockItem = block.getBlockItems().getLast();
            }
            if (blockItem instanceof Stmt stmt) {
                if (stmt.lastIsReturn()) {
                    isG = false;
                }
            }
        }

        if (isG) {
            GetSymTable.addError(block.getRbrace().getLine(), "g");
        }

        if (funcFParams != null) {
            for (FuncFParam funcFParam : funcFParams.getFuncFParams()) {
                funcFParam.checkError(s);
            }
        }

        for (BlockItem blockItem : block.getBlockItems()) {
            if (blockItem instanceof Stmt stmt) {
                stmt.checkError(s);
            } else if (blockItem instanceof VarDecl varDecl) {
                varDecl.checkError(s);
            } else if (blockItem instanceof ConstDecl constDecl) {
                constDecl.checkError(s);
            }
        }
    }

    public Function genIR() {
        Type type;
        if (funcType.is(TokenType.VOIDTK)) {
            type = new VoidType();
        } else if (funcType.is(TokenType.INTTK)) {
            type = new IntegerType(32);
        } else {
            type = new IntegerType(8);
        }
        Function function = new Function("@" + funcName.getValue(), type);
        IRBuilder.irModule.addFunction(funcName.getValue(), function);
        IRBuilder.currentFunction = function;
        IRBuilder.currentBlock = new BasicBlock(IRBuilder.getBlockName(), function);

        if (funcFParams != null) {
            function.setArguments(funcFParams.genIR(function));
            //给参数分配空间
            for (Argument argument : function.getArguments()) {
                //将非指针参数替换，参数对应的映射表
                if (!(argument.getType() instanceof PointerType)) {
                    Variable variable = new Variable(IRBuilder.getVarName(), new PointerType(argument.getType()));
                    function.changeMap(argument, variable);
                    IRBuilder.currentBlock.addInstruction(new AllocaInstr(variable));//TODO:应该没问题吧？
                    IRBuilder.currentBlock.addInstruction(new StoreInstr(argument, variable));
                }
            }
        }

        block.genIR(function);
        return function;
    }
}
