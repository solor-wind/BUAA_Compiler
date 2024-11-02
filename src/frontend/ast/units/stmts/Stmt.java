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

    public void genIR(Function function) {
        switch (kind) {
            case 1:
                Variable var = lVal.genIR(function);
                Value value = exps.get(0).genIR(function);
//                if (var.isGlobal()) {
//                    var = new Variable(var.getName(), new PointerType(var.getType()));
//                }
                if (((PointerType) (var.getType())).getBaseType().is("i8")) {
                    if (value instanceof Literal literal) {
                        value = new Literal(literal.getValue(), new IntegerType(8));
                    } else {
                        Variable newValue = new Variable(IRBuilder.getVarName(), new IntegerType(8));
                        IRBuilder.currentBlock.addInstruction(new TruncInstr(newValue, value));
                        value = newValue;
                    }
                }
                IRBuilder.currentBlock.addInstruction(new StoreInstr(value, var));
                break;
            case 2:
                if (!exps.isEmpty()) {
                    exps.get(0).genIR(function);
                }
                break;
            case 3:
                block.genIR(function);
                break;
            case 4:
                BasicBlock block1 = new BasicBlock(IRBuilder.getVarName(), function);
                IRBuilder.currentBlock = block1;
                stmt1.genIR(function);
                BasicBlock block2 = null;
                if (stmt2 != null) {
                    block2 = new BasicBlock(IRBuilder.getVarName(), function);
                    IRBuilder.currentBlock = block2;
                    stmt2.genIR(function);
                }
                ((LOrExp)cond).genIR(function,block1,block2);
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                if (exps.isEmpty()) {
                    IRBuilder.currentBlock.addInstruction(new RetInstr(null));
                } else {
                    Value v = exps.get(0).genIR(function);
                    if (!v.getType().equals(function.getType())) {
                        if (v instanceof Literal) {
                            IRBuilder.currentBlock.addInstruction(new RetInstr(new Literal(((Literal) v).getValue(), new IntegerType(8))));
                        } else {
                            Variable v2 = new Variable(IRBuilder.getVarName(), new IntegerType(8));
                            IRBuilder.currentBlock.addInstruction(new TruncInstr(v2, v));
                            IRBuilder.currentBlock.addInstruction(new RetInstr(v2));
                        }
                    } else {
                        IRBuilder.currentBlock.addInstruction(new RetInstr(v));
                    }
                }
                break;
            case 8:
                Variable var8 = lVal.genIR(function);
                Variable res8 = new Variable(IRBuilder.getVarName(), new IntegerType(32));
                IRBuilder.currentBlock.addInstruction(new CallInstr(res8, IRBuilder.getLibFunction("getint"), new ArrayList<>()));
                IRBuilder.currentBlock.addInstruction(new StoreInstr(res8, var8));
                break;
            case 9:
                Variable var9 = lVal.genIR(function);
                Variable res9 = new Variable(IRBuilder.getVarName(), new IntegerType(32));
                IRBuilder.currentBlock.addInstruction(new CallInstr(res9, IRBuilder.getLibFunction("getchar"), new ArrayList<>()));
                Variable tmp9 = new Variable(IRBuilder.getVarName(), new IntegerType(8));
                IRBuilder.currentBlock.addInstruction(new TruncInstr(tmp9, res9));
                IRBuilder.currentBlock.addInstruction(new StoreInstr(tmp9, var9));
                break;
            case 10:
                //将stringConst拆成好几个putstr+putint/putchar
                String string = stringConst.getValue();
                int index = 0, expIndex = 0;
                for (int i = 0; i < string.length(); i++) {
                    //TODO强制类型转换？
                    if (string.charAt(i) == '%' && i + 1 < string.length() && string.charAt(i + 1) == 'd') {
                        if (index != i - 1) {
                            addPutstr(string.substring(index, i));
                        }
                        Value value102 = exps.get(expIndex).genIR(function);
                        IRBuilder.currentBlock.addInstruction(new CallInstr(null, IRBuilder.getLibFunction("putint"), value102));
                        expIndex++;
                        index = i + 2;
                        i++;
                    } else if (string.charAt(i) == '%' && i + 1 < string.length() && string.charAt(i + 1) == 'c') {
                        if (index != i - 1) {
                            addPutstr(string.substring(index, i));
                        }
                        Value value102 = exps.get(expIndex).genIR(function);
                        IRBuilder.currentBlock.addInstruction(new CallInstr(null, IRBuilder.getLibFunction("putch"), value102));
                        expIndex++;
                        index = i + 2;
                        i++;
//                        if (index == i - 1) {
//                            index += 3;
//                            i += 2;
//                            Value value102 = exps.get(expIndex).genIR(function);
//                            IRBuilder.currentBlock.addInstruction(new CallInstr(null, IRBuilder.getLibFunction("putch"), value102));
//                            expIndex++;
//                            continue;
//                        }
//                        addPutstr(string.substring(index, i));
//                        //调用putch
//                        Value value102 = exps.get(expIndex).genIR(function);
//                        IRBuilder.currentBlock.addInstruction(new CallInstr(null, IRBuilder.getLibFunction("putch"), value102));
//                        index = i + 2;
//                        expIndex++;
                    }
                }
                if (index < string.length()) {
                    addPutstr(string.substring(index));
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

    public void addPutstr(String s) {
        //添加全局变量
        Variable var10 = new Variable(IRBuilder.getGlobalVarName(), new ArrayType(new IntegerType(8), s.length() + 1));
        var10.setConstant(true);
        var10.setInitValue(s);
        var10.setStringConst("c" + toWrite(s));
        IRBuilder.irModule.addGlobalVariable(var10);
        //调用str
        Variable var101 = new Variable(IRBuilder.getVarName(), new PointerType(new IntegerType(8)));
        IRBuilder.currentBlock.addInstruction(new GetPtrInstr(var101, var10, new Literal(0, new IntegerType(32))));
        IRBuilder.currentBlock.addInstruction(new CallInstr(null, IRBuilder.getLibFunction("putstr"), var101));
    }
}
