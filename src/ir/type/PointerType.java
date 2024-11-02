package ir.type;

import java.util.Objects;

public class PointerType implements Type {
    private Type baseType;

    public PointerType(Type baseType) {
        this.baseType = baseType;
    }

    public Type getBaseType() {
        return baseType;
    }

    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public boolean is(String s){
        return s.equals(baseType.toString()+"*");
    }

    @Override
    public String toString() {
        return baseType.toString() + "*";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PointerType) {
            return baseType.equals(((PointerType) obj).getBaseType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseType, "*");
    }
}
