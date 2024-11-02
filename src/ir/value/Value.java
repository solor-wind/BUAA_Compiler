package ir.value;

import ir.type.Type;

import java.util.Objects;

public class Value {
    private String name;
    private Type type;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    /**
     * 返回是否是某类型
     * Array,
     */
//    public boolean isType(String s){
//
//    }

    @Override
    public String toString() {
        return type + " " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Value value) {
            return name.equals(value.name) && type.equals(value.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

}
