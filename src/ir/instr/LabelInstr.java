package ir.instr;

import ir.value.BasicBlock;
import ir.value.Instruction;

public class LabelInstr extends Instruction {
    private BasicBlock label;

    public LabelInstr(BasicBlock label) {
        super("label");
        this.label = label;
    }

    public BasicBlock getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label.getName() + ":";
    }
}
