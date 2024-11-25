package ir.instr;

import ir.type.PointerType;
import ir.value.Instruction;
import ir.value.Variable;

public class AllocaInstr extends Instruction {
    private Variable var;
    private int size;

    public AllocaInstr(Variable var, int size) {
        super("alloca");
        this.var = var;
        this.size = size;
        defs.add(var);
    }

    public AllocaInstr(Variable var) {
        super("alloca");
        this.var = var;
        this.size = 1;
        defs.add(var);
    }

    public Variable getVar() {
        return var;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        String string = var.getName() + " = alloca " + ((PointerType) var.getType()).getBaseType();
        if (size > 1) {
            string += ", i32 " + size;
        }
        return string;
    }

}
