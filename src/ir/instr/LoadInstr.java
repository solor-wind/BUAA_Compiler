package ir.instr;

import ir.value.Instruction;
import ir.value.Variable;

public class LoadInstr extends Instruction {
    Variable res;
    Variable addr;

    public LoadInstr(Variable res, Variable addr) {
        super("load");
        this.res = res;
        defs.add(res);
        this.addr = addr;
        uses.add(addr);
    }

    public Variable getRes() {
        return res;
    }

    public Variable getAddr() {
        return addr;
    }

    @Override
    public String toString() {
        return res.getName() + " = load " + res.getType() + ", " + addr;
    }
}
