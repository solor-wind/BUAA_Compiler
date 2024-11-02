package ir.instr;

import ir.value.Instruction;
import ir.value.Variable;

public class LoadInstr extends Instruction {
    Variable res;
    Variable addr;

    public LoadInstr(Variable res, Variable addr) {
        super("load");
        this.res = res;
        this.addr = addr;
    }

    @Override
    public String toString() {
        return res.getName() + " = load " + res.getType() + ", " + addr;
    }
}
