package frontend.ast.units.stmts;

import frontend.ast.units.exps.LOrExp;
import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.symbols.GetSymTable;
import frontend.symbols.Symbol;
import frontend.symbols.SymbolTable;
import ir.IRBuilder;
import ir.instr.*;
import ir.type.ArrayType;
import ir.type.IntegerType;
import ir.type.PointerType;
import ir.value.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class Stmt implements BlockItem {
    private Token keyword;//if,for,break,continue,return,getint,getchar,printf
    private ArrayList<Token> idents = new ArrayList<>();

    private LVal lVal = null;
    private LinkedList<Exp> exps = new LinkedList<>();
    private Block block = null;

    private Cond cond;
    private Stmt stmt1;
    private Stmt stmt2;

    private ForStmt forStmt1;
    private ForStmt forStmt2;

    private Token stringConst;

    public Stmt(Token keyword) {
        this.keyword = keyword;
        idents = new ArrayList<>();
        exps = new LinkedList<>();
    }

    public void setKeyword(Token keyword) {
        this.keyword = keyword;
    }

    public void setLVal(LVal lVal) {
        this.lVal = lVal;
    }

    public void addIdent(Token ident) {
        idents.add(ident);
    }

    public void addExp(Exp exp) {
        exps.add(exp);
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setCond(Cond cond) {
        this.cond = cond;
    }

    public void setStmt1(Stmt stmt1) {
        this.stmt1 = stmt1;
    }

    public void setStmt2(Stmt stmt2) {
        this.stmt2 = stmt2;
    }

    public void setForStmt1(ForStmt forStmt1) {
        this.forStmt1 = forStmt1;
    }

    public void setForStmt2(ForStmt forStmt2) {
        this.forStmt2 = forStmt2;
    }

    public void setStringConst(Token stringConst) {
        this.stringConst = stringConst;
    }

    public boolean lastIsReturn() {
        if (keyword != null && keyword.is(TokenType.RETURNTK)) {
            return true;
        } else if (block != null) {
            BlockItem blockItem;
            if (block.getBlockItems().isEmpty()) {
                blockItem = null;
            } else {
                blockItem = block.getBlockItems().getLast();
            }
            if (blockItem instanceof Stmt stmt) {
                return stmt.lastIsReturn();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String string = "<Stmt>";
        if (keyword == null) {
            if (lVal != null) {
                return lVal.toString() + "\n" + idents.get(0) + "\n"
                        + exps.getFirst() + "\n" + idents.get(1) + "\n" + string;
            } else if (block != null) {
                return block + "\n" + string;
            } else {
                if (exps.isEmpty()) {
                    return idents.get(0) + "\n" + string;
                } else {
                    return exps.getFirst() + "\n" + idents.get(0) + "\n" + string;
                }
            }
        }
        String ret = "";
        switch (keyword.getToken()) {
            case IFTK:
                ret = keyword + "\n" + idents.get(0) + "\n" + cond + "\n" +
                        idents.get(1) + "\n" + stmt1 + "\n";
                if (idents.size() > 2) {
                    ret += idents.get(2) + "\n" + stmt2 + "\n";
                }
                break;
            case FORTK:
                ret = keyword + "\n" + idents.get(0) + "\n";
                if (forStmt1 != null) {
                    ret += forStmt1.toString() + "\n";

                }
                ret += idents.get(1) + "\n";
                if (cond != null) {
                    ret += cond.toString() + "\n";
                }
                ret += idents.get(2) + "\n";
                if (forStmt2 != null) {
                    ret += forStmt2.toString() + "\n";
                }
                ret += idents.get(3) + "\n" + stmt1 + "\n";
                break;
            case BREAKTK, CONTINUETK:
                ret = keyword + "\n" + idents.get(0) + "\n";
                break;
            case RETURNTK:
                if (exps.isEmpty()) {
                    ret = keyword + "\n" + idents.get(0) + "\n";
                } else {
                    ret = keyword + "\n" + exps.getFirst() + "\n" + idents.get(0) + "\n";
                }
                break;
            case GETINTTK, GETCHARTK:
                ret = lVal + "\n" + idents.get(0) + "\n" + keyword + "\n" + idents.get(1) + "\n" + idents.get(2) + "\n" + idents.get(3) + "\n";
                break;
            default: {
                StringBuilder sb = new StringBuilder();
                sb.append(keyword + "\n" + idents.get(0) + "\n" + stringConst + "\n");
                int i = 1;
                Iterator<Exp> it = exps.iterator();
                while (it.hasNext()) {
                    sb.append(idents.get(i) + "\n" + it.next() + "\n");
                    i++;
                }
                ret += sb.toString() + idents.get(i) + "\n" + idents.get(i + 1) + "\n";
            }
        }
        return ret + string;
    }

    public void checkError(SymbolTable symbolTable) {
        //error h
        if (lVal != null) {
            if (!lVal.checkError(symbolTable)) {
                Symbol symbol = symbolTable.getSymbol(lVal.getIdent().getValue());
                if (symbol.is("Const")) {
                    GetSymTable.addError(lVal.getIdent().getLine(), "h");
                }
            }
        }

        //error l
        if (keyword != null && keyword.is(TokenType.PRINTFTK)) {
            String string = stringConst.getValue();
            int index = 0;
            int count = 0;
            while ((index = string.indexOf("%d", index)) != -1) {
                count++;
                index += 2;
            }
            while ((index = string.indexOf("%c", index)) != -1) {
                count++;
                index += 2;
            }
            if (count != exps.size()) {
                GetSymTable.addError(keyword.getLine(), "l");
            }
        }

        //error f
        if (keyword != null && keyword.is(TokenType.RETURNTK)) {
            if (symbolTable.getFuncSym().is("Void") && !exps.isEmpty()) {
                GetSymTable.addError(keyword.getLine(), "f");
            }
        }

        if (block != null) {
            block.checkError(symbolTable);
        }

        if (!exps.isEmpty()) {
            for (Exp exp : exps) {
                exp.checkError(symbolTable);
            }
        }

        if (stmt1 != null) {
            stmt1.checkError(symbolTable);
        }

        if (stmt2 != null) {
            stmt2.checkError(symbolTable);
        }

        if (forStmt1 != null) {
            forStmt1.checkError(symbolTable);
        }

        if (forStmt2 != null) {
            forStmt2.checkError(symbolTable);
        }

        if (cond != null) {
            ((LOrExp) cond).checkError(symbolTable);
        }
    }

    /**
     * 1: LVal '=' Exp ';'
     * 2: [Exp] ';'
     * 3: Block
     * 4: 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
     * 5: 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
     * 6: 'break' ';' | 'continue' ';'
     * 7: 'return' [Exp] ';'
     * 8: LVal '=' 'getint''('')'';'
     * 9: LVal '=' 'getchar''('')'';'
     * 10:'printf''('StringConst {','Exp}')'';'
     */
    private int kind = -1;

    public void setKind(int kind) {
        this.kind = kind;
    }

    public void genIR(Function function, BasicBlock basicBlock) {
        switch (kind) {
            case 1:
                Variable var = lVal.genIR(function, basicBlock);
                Value value = exps.get(0).genIR(function, basicBlock);
                value = IRBuilder.changeType(basicBlock, value, ((PointerType) (var.getType())).getBaseType());
                basicBlock.addInstruction(new StoreInstr(value, var));
                break;
            case 2:
                if (!exps.isEmpty()) {
                    exps.get(0).genIR(function, basicBlock);
                }
                break;
            case 3:
                block.genIR(function, basicBlock);
                break;
            case 4:
                BasicBlock block1 = new BasicBlock(IRBuilder.getBlockName(), function);
                BasicBlock block2 = null;
                if (stmt2 != null) {
                    block2 = new BasicBlock(IRBuilder.getBlockName(), function);
                }
                //提供成功、失败要跳转到的块
                //获取用于分支判断的一堆块
                LinkedList<BasicBlock> blocks = ((LOrExp) cond).genIR(function, block1, block2);
                stmt1.genIR(function, block1);
                if (stmt2 != null) {
                    stmt2.genIR(function, block2);
                }
                //跳入分支判断
                basicBlock.addInstruction(new BrInstr(blocks.getFirst()));
                //返回原来块的入口，以及分割时所用的标签
                BasicBlock bin = new BasicBlock(IRBuilder.getBlockName(), function);
                basicBlock.addInstruction(new LabelInstr(bin));
                //返回入口
                block1.addInstruction(new BrInstr(bin));
                if (stmt2 != null) {
                    block2.addInstruction(new BrInstr(bin));
                    blocks.getLast().addInstruction(new BrInstr(block2));
                } else {
                    blocks.getLast().addInstruction(new BrInstr(bin));
                }
                IRBuilder.connectBlock(blocks.getLast(), block1);//分支判断连接b1
                blocks.add(block1);
                if (stmt2 != null) {
                    blocks.add(block2);
                    IRBuilder.connectBlock(block1, block2);//分支判断连接b2
                }
                //加入map，等待后续处理
                //function.addBlocks(blocks);
                break;
            case 5:
                if (forStmt1 != null) {
                    forStmt1.genIR(function, basicBlock);
                }
                BasicBlock bin2 = new BasicBlock(IRBuilder.getBlockName(), function);
                BasicBlock stmtBlock = new BasicBlock(IRBuilder.getBlockName(), function);

                LinkedList<BasicBlock> blocks2 = null;
                if (cond != null) {
                    blocks2 = ((LOrExp) cond).genIR(function, stmtBlock, bin2);
                    basicBlock.addInstruction(new BrInstr(blocks2.getFirst()));//跳入分支判断
                    basicBlock.addInstruction(new LabelInstr(bin2));
                    blocks2.getLast().addInstruction(new BrInstr(bin2));//回来
                    IRBuilder.connectBlock(blocks2.getLast(), stmtBlock);//分支判断连接语句
                } else {
                    basicBlock.addInstruction(new BrInstr(stmtBlock));//直接跳入语句
                    basicBlock.addInstruction(new LabelInstr(bin2));
                }
                BasicBlock forStmt2Block = new BasicBlock(IRBuilder.getBlockName(), function);
                IRBuilder.connectBlock(stmtBlock, forStmt2Block);//连接语句和forStmt2

//                LinkedList<BasicBlock> blocks2 = ((LOrExp) cond).genIR(function, stmtBlock, bin2);
//                basicBlock.addInstruction(new BrInstr(blocks2.getFirst()));//跳入分支判断
//                basicBlock.addInstruction(new LabelInstr(bin2));
//                blocks2.getLast().addInstruction(new BrInstr(bin2));//回来
//                IRBuilder.connectBlock(blocks2.getLast(), stmtBlock);//分支判断连接语句
//                BasicBlock forStmt2Block = new BasicBlock(IRBuilder.getBlockName(), function);
//                IRBuilder.connectBlock(stmtBlock, forStmt2Block);//连接语句和forStmt2

                ArrayList<BasicBlock> forList = new ArrayList<>();
//                forList.add(blocks2.getFirst());
//                forList.add(stmtBlock);
                forList.add(forStmt2Block);
                forList.add(bin2);
                IRBuilder.forBlock.push(forList);

                stmt1.genIR(function, stmtBlock);//需要提前将当前循环信息压入栈
                stmtBlock.addInstruction(new BrInstr(forStmt2Block));
                IRBuilder.forBlock.pop();
                if (forStmt2 != null) {
                    forStmt2.genIR(function, forStmt2Block);
                }
                if (cond != null) {
                    forStmt2Block.addInstruction(new BrInstr(blocks2.getFirst()));
                } else {
                    forStmt2Block.addInstruction(new BrInstr(stmtBlock));
                }


                break;
            case 6:
                if (keyword.is(TokenType.BREAKTK)) {
                    basicBlock.addInstruction(new BrInstr(IRBuilder.forBlock.peek().get(1)));
                } else {
                    basicBlock.addInstruction(new BrInstr(IRBuilder.forBlock.peek().get(0)));
                }
                break;
            case 7:
                if (exps.isEmpty()) {
                    basicBlock.addInstruction(new RetInstr(null));
                } else {
                    Value v = exps.get(0).genIR(function, basicBlock);
                    v = IRBuilder.changeType(basicBlock, v, function.getType());
                    basicBlock.addInstruction(new RetInstr(v));
                }
                break;
            case 8:
                Variable var8 = lVal.genIR(function, basicBlock);
                Variable res8 = new Variable(IRBuilder.getVarName(), new IntegerType(32));
                basicBlock.addInstruction(new CallInstr(res8, IRBuilder.getLibFunction("getint"), new ArrayList<>()));
                basicBlock.addInstruction(new StoreInstr(res8, var8));
                break;
            case 9:
                Variable var9 = lVal.genIR(function, basicBlock);
                Variable res9 = new Variable(IRBuilder.getVarName(), new IntegerType(32));
                basicBlock.addInstruction(new CallInstr(res9, IRBuilder.getLibFunction("getchar"), new ArrayList<>()));
                Variable tmp9 = (Variable) IRBuilder.changeType(basicBlock, res9, ((PointerType) var9.getType()).getBaseType());
                basicBlock.addInstruction(new StoreInstr(tmp9, var9));
                break;
            case 10:
                //将stringConst拆成好几个putstr+putint/putchar
                String string = stringConst.getValue();
                int index = 0, expIndex = 0;
                for (int i = 0; i < string.length(); i++) {
                    if (string.charAt(i) == '%' && i + 1 < string.length() && string.charAt(i + 1) == 'd') {
                        if (index != i) {
                            addPutstr(string.substring(index, i), basicBlock);
                        }
                        Value value102 = exps.get(expIndex).genIR(function, basicBlock);
                        value102 = IRBuilder.changeType(basicBlock, value102, new IntegerType(32));
                        basicBlock.addInstruction(new CallInstr(null, IRBuilder.getLibFunction("putint"), value102));
                        expIndex++;
                        index = i + 2;
                        i++;
                    } else if (string.charAt(i) == '%' && i + 1 < string.length() && string.charAt(i + 1) == 'c') {
                        if (index != i) {
                            addPutstr(string.substring(index, i), basicBlock);
                        }
                        Value value102 = exps.get(expIndex).genIR(function, basicBlock);
                        value102 = IRBuilder.changeType(basicBlock, value102, new IntegerType(32));
                        basicBlock.addInstruction(new CallInstr(null, IRBuilder.getLibFunction("putch"), value102));
                        expIndex++;
                        index = i + 2;
                        i++;
                    }
                }
                if (index < string.length()) {
                    addPutstr(string.substring(index), basicBlock);
                }
                break;

        }
    }

    public String toWrite(String string) {
        String res = string.replace(Character.toString(92), "\\\\");
        res = res.replace(Character.toString(7), "\\a");
        res = res.replace(Character.toString(8), "\\b");
        res = res.replace(Character.toString(9), "\\t");
        res = res.replace(Character.toString(10), "\\0A");
        res = res.replace(Character.toString(11), "\\v");
        res = res.replace(Character.toString(12), "\\f");
        res = res.replace(Character.toString(34), "\\\"");
        res = res.replace(Character.toString(39), "\\'");
        res = res.replace(Character.toString(0), "\\0");
        return "\"" + res + "\\00\"";
    }

    public String toMips(String string){
        String res = string.replace(Character.toString(92), "\\\\");
        res = res.replace(Character.toString(7), "\\a");
        res = res.replace(Character.toString(8), "\\b");
        res = res.replace(Character.toString(9), "\\t");
        res = res.replace(Character.toString(10), "\\n");
        res = res.replace(Character.toString(11), "\\v");
        res = res.replace(Character.toString(12), "\\f");
        res = res.replace(Character.toString(34), "\\\"");
        res = res.replace(Character.toString(39), "\\'");
        return "\"" + res + "\"";
    }

    public void addPutstr(String s, BasicBlock basicBlock) {
        if(s.equals(" ")){
            basicBlock.addInstruction(new CallInstr(null, IRBuilder.getLibFunction("putch"), new Literal(32,new IntegerType(32))));
            return;
        } else if (s.equals("\n")) {
            basicBlock.addInstruction(new CallInstr(null, IRBuilder.getLibFunction("putch"), new Literal(10,new IntegerType(32))));
            return;
        }
        if(s.isEmpty()){
            return;
        }
        //添加全局变量
        Variable var10 = new Variable(IRBuilder.getGlobalVarName(), new ArrayType(new IntegerType(8), s.length() + 1));
        var10.setConstant(true);
        var10.setInitValue(toMips(s));
        var10.setStringConst("c" + toWrite(s));
        IRBuilder.irModule.addGlobalVariable(var10);
        //调用str
        Variable var101 = new Variable(IRBuilder.getVarName(), new PointerType(new IntegerType(8)));
        basicBlock.addInstruction(new GetPtrInstr(var101, var10, new Literal(0, new IntegerType(32))));
        basicBlock.addInstruction(new CallInstr(null, IRBuilder.getLibFunction("putstr"), var101));
    }
}
