package ir.value;

import ir.type.OtherType;

import java.util.HashSet;

public class Instruction extends Value {
    public HashSet<Value> uses = new HashSet<>();
    public HashSet<Value> defs = new HashSet<>();

    public Instruction(String name) {
        super(name, new OtherType("instruction"));
    }
}
