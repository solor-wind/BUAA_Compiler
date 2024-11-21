package ir.instr;

import ir.value.BasicBlock;
import ir.value.Instruction;
import ir.value.Value;

public class BrInstr extends Instruction {
    private Value cond = null;
    private BasicBlock block1;
    private BasicBlock block2 = null;

    public BrInstr(Value cond, BasicBlock block1, BasicBlock block2) {
        super("br");
        this.cond = cond;
        this.block1 = block1;
        this.block2 = block2;
    }

    public BrInstr(BasicBlock block1) {
        super("br");
        this.block1 = block1;
    }

    public Value getCond() {
        return cond;
    }

    public void setCond(Value cond) {
        this.cond = cond;
    }

    public BasicBlock getBlock1() {
        return block1;
    }

    public BasicBlock getBlock2() {
        return block2;
    }

    @Override
    public String toString() {
        if (cond != null) {
            return "br " + cond + ", label %" + block1.getName() + ", label %" + block2.getName();
        } else {
            return "br label %" + block1.getName();
        }
    }
}
