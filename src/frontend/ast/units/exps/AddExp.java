package frontend.ast.units.exps;

import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.symbols.SymbolTable;
import ir.IRBuilder;
import ir.instr.BinaInstr;
import ir.type.IntegerType;
import ir.type.Type;
import ir.value.*;

import java.util.Iterator;
import java.util.LinkedList;

public class AddExp {
    //左递归文法改写
    LinkedList<MulExp> mulExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addmulExp(MulExp mulExp) {
        mulExps.add(mulExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<MulExp> it1 = mulExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<AddExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString();
    }

    public boolean checkError(SymbolTable symbolTable) {
        boolean flag = false;
        for (MulExp mulExp : mulExps) {
            flag = flag || mulExp.checkError(symbolTable);
        }
        return flag;
    }

    public int evaluate(SymbolTable symbolTable) {
        Iterator<MulExp> it1 = mulExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        int ans = 0;
        if (it1.hasNext()) {
            ans += it1.next().evaluate(symbolTable);
        }
        while (it1.hasNext() && it2.hasNext()) {
            MulExp mulExp = it1.next();
            Token op = it2.next();
            if (op.is(TokenType.MINU)) {
                ans -= mulExp.evaluate(symbolTable);
            } else {
                ans += mulExp.evaluate(symbolTable);
            }
        }
        return ans;
    }

    public String getType() {
        return mulExps.getFirst().getType();
    }

    public Value genIR(Function function, BasicBlock basicBlock) {
        Iterator<MulExp> it1 = mulExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        Value value = null;
        if (it1.hasNext()) {
            value = it1.next().genIR(function, basicBlock);
        }
        //TODO:更好的算法是先把字面量算出来
        while (it2.hasNext() && it1.hasNext()) {
            Value v2 = it1.next().genIR(function, basicBlock);
            if (value instanceof Literal l1 && v2 instanceof Literal l2) {
                if (it2.next().is(TokenType.PLUS)) {
                    value = new Literal(l1.getValue() + l2.getValue(), new IntegerType(32));
                } else {
                    value = new Literal(l1.getValue() - l2.getValue(), new IntegerType(32));
                }
            } else {
                Variable var = new Variable(IRBuilder.getVarName(), new IntegerType(32));
                if (it2.next().is(TokenType.PLUS)) {
                    basicBlock.addInstruction(new BinaInstr("add", var, value, v2));
                } else {
                    basicBlock.addInstruction(new BinaInstr("sub", var, value, v2));
                }
                value = var;
            }
        }
        return value;
    }
}
