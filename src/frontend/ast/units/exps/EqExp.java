package frontend.ast.units.exps;

import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.symbols.SymbolTable;
import ir.IRBuilder;
import ir.instr.IcmpInstr;
import ir.instr.ZextInstr;
import ir.type.IntegerType;
import ir.value.*;

import java.util.Iterator;
import java.util.LinkedList;

public class EqExp {
    //左递归文法改写
    LinkedList<RelExp> relExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addRelExp(RelExp relExp) {
        relExps.add(relExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<RelExp> it1 = relExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<EqExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString();
    }

    public void checkError(SymbolTable symbolTable) {
        for (RelExp relExp : relExps) {
            relExp.checkError(symbolTable);
        }
    }

    /**
     * 返回i1
     */
    public Value genIR(Function function, BasicBlock basicBlock) {
        Iterator<RelExp> it1 = relExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        Value value1 = null;
        if (it1.hasNext()) {
            value1 = it1.next().genIR(function, basicBlock);
        }
        while (it1.hasNext() && it2.hasNext()) {
            Value value2 = it1.next().genIR(function, basicBlock);
            String op = it2.next().is(TokenType.EQL) ? "eq" : "ne";
            if (value1 instanceof Literal l1 && value2 instanceof Literal l2) {
                if (op.equals("eq")) {
                    value1 = new Literal(l1.getValue() == l2.getValue() ? 1 : 0, new IntegerType(1));
                } else {
                    value1 = new Literal(l1.getValue() != l2.getValue() ? 1 : 0, new IntegerType(1));
                }
            } else {
                if (value1.getType().equals(value2.getType())) {
                    Variable v1 = new Variable(IRBuilder.getVarName(), new IntegerType(1));
                    basicBlock.addInstruction(new IcmpInstr(v1, value1, value2, op));
                    value1 = v1;
                } else if (value1 instanceof Literal l1) {
                    Variable v1 = new Variable(IRBuilder.getVarName(), new IntegerType(1));
                    basicBlock.addInstruction(new IcmpInstr(v1, new Literal(l1.getValue(), value2.getType()), value2, op));
                    value1 = v1;
                } else if (value2 instanceof Literal l2) {
                    Variable v1 = new Variable(IRBuilder.getVarName(), new IntegerType(1));
                    basicBlock.addInstruction(new IcmpInstr(v1, value1, new Literal(l2.getValue(), value1.getType()), op));
                    value1 = v1;
                } else {
                    Variable tmp = new Variable(IRBuilder.getVarName(), new IntegerType(32));
                    Variable v1 = new Variable(IRBuilder.getVarName(), new IntegerType(1));
                    if (((IntegerType) value1.getType()).getBits() < ((IntegerType) value2.getType()).getBits()) {
                        basicBlock.addInstruction(new ZextInstr(tmp, value1));
                        basicBlock.addInstruction(new IcmpInstr(v1, tmp, value2, op));
                    } else {
                        basicBlock.addInstruction(new ZextInstr(tmp, value2));
                        basicBlock.addInstruction(new IcmpInstr(v1, value1, tmp, op));
                    }
                    value1 = v1;
                }
            }
        }
        if (!value1.getType().equals(new IntegerType(1))) {
            if (value1 instanceof Literal l) {
                value1 = new Literal(l.getValue() == 0 ? 0 : 1, new IntegerType(1));
            } else {
                Variable var = new Variable(IRBuilder.getVarName(), new IntegerType(1));
                basicBlock.addInstruction(new IcmpInstr(var, value1, new Literal(0, value1.getType()), "ne"));
                value1 = var;
            }
        }
        return value1;
    }
}
