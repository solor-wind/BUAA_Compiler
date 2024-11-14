package frontend.ast.units.exps;

import frontend.lexer.Token;
import frontend.symbols.SymbolTable;
import ir.IRBuilder;
import ir.instr.BrInstr;
import ir.value.BasicBlock;
import ir.value.Function;
import ir.value.Literal;
import ir.value.Value;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LAndExp {
    //左递归文法改写
    LinkedList<EqExp> eqExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addEqExp(EqExp eqExp) {
        eqExps.add(eqExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<EqExp> it1 = eqExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<LAndExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString();
    }

    public void checkError(SymbolTable symbolTable) {
        for (EqExp eqExp : eqExps) {
            eqExp.checkError(symbolTable);
        }
    }

    public LinkedList<BasicBlock> genIR(Function function, BasicBlock basicBlock, BasicBlock block1, BasicBlock block2) {
        LinkedList<BasicBlock> blocks = new LinkedList<>();
        blocks.add(basicBlock);
        for (EqExp eqExp : eqExps) {
            Value value = eqExp.genIR(function, blocks.getLast());
            BasicBlock newblock = new BasicBlock(IRBuilder.getBlockName(), function);
            if (value instanceof Literal l) {
                if (l.getValue() == 0) {
                    blocks.getLast().addInstruction(new BrInstr(block2));
                } else {
                    blocks.getLast().addInstruction(new BrInstr(newblock));
                }
            } else {
                blocks.getLast().addInstruction(new BrInstr(value, newblock, block2));
            }
            blocks.add(newblock);
        }
        blocks.getLast().addInstruction(new BrInstr(block1));
        blocks.removeFirst();
        return blocks;
    }
}
