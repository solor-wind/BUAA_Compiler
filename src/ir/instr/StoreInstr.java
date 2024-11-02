package ir.instr;

import ir.value.Instruction;
import ir.value.Value;

public class StoreInstr extends Instruction {
    Value value;//Variable,Literal,
    Value addr;//Variable(pointer)

    public StoreInstr(Value value, Value addr) {
        super("store");
        this.value = value;
        this.addr = addr;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "store " + value + ", " + addr;
    }
}
