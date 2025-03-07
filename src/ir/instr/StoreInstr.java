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
        uses.add(value);
        uses.add(addr);
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        uses.remove(this.value);
        uses.add(value);
        this.value = value;
    }

    public Value getAddr() {
        return addr;
    }

    @Override
    public String toString() {
        return "store " + value + ", " + addr;
    }
}
