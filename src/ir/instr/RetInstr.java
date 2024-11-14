package ir.instr;

import ir.value.Instruction;
import ir.value.Value;

public class RetInstr extends Instruction {
    Value value;

    public RetInstr(Value value) {
        super("ret");
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (value == null) {
            return "ret void";
        }
        return "ret " + value.toString();
    }
}
