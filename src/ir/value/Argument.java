package ir.value;

import ir.type.Type;

public class Argument extends Value {
    public Value value;
    private boolean isArray = false;

    public Argument(String name, Type type) {
        super(name, type);
    }

    public Argument(Value value) {
        super(value.getName(), value.getType());
        this.value = value;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean isArray) {
        this.isArray = isArray;
    }

    @Override
    public String toString() {
        return getType() + " " + getName();
    }
}
