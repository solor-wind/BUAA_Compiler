package frontend.ast.units.exps;

import frontend.lexer.Token;
import frontend.symbols.SymbolTable;
import ir.IRBuilder;
import ir.instr.IcmpInstr;
import ir.instr.ZextInstr;
import ir.type.IntegerType;
import ir.value.*;

import java.util.Iterator;
import java.util.LinkedList;

public class RelExp {
    //左递归文法改写
    LinkedList<AddExp> addExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addAddExp(AddExp addExp) {
        addExps.add(addExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<AddExp> it1 = addExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<RelExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString();
    }

    public void checkError(SymbolTable symbolTable) {
        for (AddExp addExp : addExps) {
            addExp.checkError(symbolTable);
        }
    }

    /**
     * 可能返回i1，也可能返回i32
     */
    public Value genIR(Function function, BasicBlock basicBlock) {
        if (addExps.size() == 1) {
            return addExps.get(0).genIR(function, basicBlock);
        }
        Iterator<AddExp> it1 = addExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        Value value1 = null;
        if (it1.hasNext()) {
            value1 = it1.next().genIR(function, basicBlock);
        }
        while (it1.hasNext() && it2.hasNext()) {
            Value value2 = it1.next().genIR(function, basicBlock);
            String op = switch (it2.next().getToken()) {
                case LSS -> "slt";
                case LEQ -> "sle";
                case GRE -> "sgt";
                case GEQ -> "sge";
                default -> "";
            };
            if (value1 instanceof Literal l1 && value2 instanceof Literal l2) {
                value1 = switch (op) {
                    case "slt" -> new Literal(l1.getValue() < l2.getValue() ? 1 : 0, new IntegerType(1));
                    case "sle" -> new Literal(l1.getValue() <= l2.getValue() ? 1 : 0, new IntegerType(1));
                    case "sgt" -> new Literal(l1.getValue() > l2.getValue() ? 1 : 0, new IntegerType(1));
                    default -> new Literal(l1.getValue() >= l2.getValue() ? 1 : 0, new IntegerType(1));
                };
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
                } else {
                    Variable tmp = new Variable(IRBuilder.getVarName(), value2.getType());
                    basicBlock.addInstruction(new ZextInstr(tmp, value1));
                    Variable v1 = new Variable(IRBuilder.getVarName(), new IntegerType(1));
                    basicBlock.addInstruction(new IcmpInstr(v1, tmp, value2, op));
                    value1 = v1;
                }
            }
        }
        return value1;
    }
}
