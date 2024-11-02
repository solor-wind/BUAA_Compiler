package ir.value;

import ir.type.OtherType;
import ir.type.Type;

public class Instruction extends Value {
    public Instruction(String name) {
        super(name, new OtherType("instruction"));
    }
}
