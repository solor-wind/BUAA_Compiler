package ir.value;

import ir.type.Type;

public class Argument extends Value {
    public Value value;

    public Argument(String name, Type type) {
        super(name, type);
    }

    public Argument(Value value) {
        super(value.getName(), value.getType());
        this.value = value;
    }

    @Override
    public String toString() {
        return getType()+" "+getName();
    }
}
