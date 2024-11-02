package ir.value;

import ir.type.Type;

public class Literal extends Value {
    private int value;

    public Literal(int value, Type type) {
        super(Integer.toString(value), type);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getType().toString()+" "+getName();
    }
}
