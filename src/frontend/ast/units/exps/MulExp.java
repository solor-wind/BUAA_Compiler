package frontend.ast.units.exps;

import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.symbols.SymbolTable;
import ir.IRBuilder;
import ir.instr.BinaInstr;
import ir.type.IntegerType;
import ir.value.Function;
import ir.value.Literal;
import ir.value.Value;
import ir.value.Variable;

import java.util.Iterator;
import java.util.LinkedList;

public class MulExp {
    //左递归文法改写
    LinkedList<UnaryExp> unaryExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addUnaryExp(UnaryExp unaryExp) {
        unaryExps.add(unaryExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<UnaryExp> it1 = unaryExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<MulExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString();
    }

    public boolean checkError(SymbolTable symbolTable) {
        boolean flag = false;
        for (UnaryExp unaryExp : unaryExps) {
            flag = flag || unaryExp.checkError(symbolTable);
        }
        return flag;
    }

    public int evaluate(SymbolTable symbolTable) {
        int ans = 1;
        for (UnaryExp unaryExp : unaryExps) {
            ans *= unaryExp.evaluate(symbolTable);
        }
        return ans;
    }

    public String getType() {
        return unaryExps.getFirst().getType();
    }

    public Value genIR(Function function) {
        Iterator<UnaryExp> it1 = unaryExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        Value value = null;
        if (it1.hasNext()) {
            value = it1.next().genIR(function);
        }
        while (it2.hasNext() && it1.hasNext()) {
            Value v2 = it1.next().genIR(function);
            Token op = it2.next();
            if (value instanceof Literal l1 && v2 instanceof Literal l2) {
                if (op.is(TokenType.MULT)) {
                    value = new Literal(l1.getValue() * l2.getValue(), new IntegerType(32));
                } else if (op.is(TokenType.DIV)) {
                    value = new Literal(l1.getValue() / l2.getValue(), new IntegerType(32));
                } else {
                    value = new Literal(l1.getValue() % l2.getValue(), new IntegerType(32));
                }
            } else {
                Variable var = new Variable(IRBuilder.getVarName(), new IntegerType(32));
                if (op.is(TokenType.MULT)) {
                    IRBuilder.currentBlock.addInstruction(new BinaInstr("mul", var, value, v2));
                } else if (op.is(TokenType.DIV)) {
                    IRBuilder.currentBlock.addInstruction(new BinaInstr("div", var, value, v2));
                } else {
                    IRBuilder.currentBlock.addInstruction(new BinaInstr("srem", var, value, v2));
                }
                value = var;
            }
        }
        return value;
    }
}
