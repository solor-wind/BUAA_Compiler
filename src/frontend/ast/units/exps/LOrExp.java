package frontend.ast.units.exps;

import frontend.ast.units.stmts.Cond;
import frontend.lexer.Token;
import frontend.symbols.SymbolTable;
import ir.IRBuilder;
import ir.instr.BrInstr;
import ir.instr.LabelInstr;
import ir.value.BasicBlock;
import ir.value.Function;
import ir.value.Instruction;
import ir.value.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class LOrExp extends Cond {
    //左递归文法改写
    LinkedList<LAndExp> lAndExps = new LinkedList<>();
    LinkedList<Token> ops = new LinkedList<>();

    public void addLAndExp(LAndExp lAndExp) {
        lAndExps.add(lAndExp);
    }

    public void addOp(Token op) {
        ops.add(op);
    }

    @Override
    public String toString() {
        Iterator<LAndExp> it1 = lAndExps.iterator();
        Iterator<Token> it2 = ops.iterator();
        StringBuffer sb = new StringBuffer();
        while (it1.hasNext()) {
            sb.append(it1.next() + "\n<LOrExp>");
            if (it2.hasNext()) {
                sb.append("\n" + it2.next() + "\n");
            }
        }
        return sb.toString() + "\n<Cond>";
    }

    public void checkError(SymbolTable symbolTable) {
        for (LAndExp lAndExp : lAndExps) {
            lAndExp.checkError(symbolTable);
        }
    }

    public LinkedList<BasicBlock> genIR(Function function, BasicBlock block1, BasicBlock block2) {
        //a||b||c
        //if a ->
        LinkedList<BasicBlock> blocks = new LinkedList<>();
        blocks.add(new BasicBlock(IRBuilder.getBlockName(), function));

        for (LAndExp lAndExp : lAndExps) {
            //在blocks.getLast()做判断，真去block，假去newblock
            BasicBlock newblock = new BasicBlock(IRBuilder.getBlockName(), function);
            blocks.addAll(lAndExp.genIR(function, blocks.getLast(), block1, newblock));
            //blocks.getLast().addInstruction(new BrInstr(value, block1, newblock));
            blocks.addLast(newblock);
        }

        Iterator<BasicBlock> it1 = blocks.iterator();
        BasicBlock b1 = it1.next();
        while (it1.hasNext()) {
            BasicBlock b2 = it1.next();
            IRBuilder.connectBlock(b1,b2);
            b1=b2;
        }

        return blocks;
    }
}
