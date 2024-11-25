package ir.instr;

import ir.value.Instruction;
import ir.value.Value;

public class RetInstr extends Instruction {
    Value value;

    public RetInstr(Value value) {
        super("ret");
        this.value = value;
        if (value != null) {
            uses.add(value);
        }
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        uses.remove(this.value);
        uses.add(value);
        this.value = value;
    }

    @Override
    public String toString() {
        if (value == null) {
            return "ret void";
        }
        return "ret " + value.toString();
    }
}
