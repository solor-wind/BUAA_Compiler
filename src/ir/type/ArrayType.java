package ir.type;

import java.util.Objects;

public class ArrayType implements Type {
    private Type elementType;
    private int length;

    public ArrayType(Type elementType, int length) {
        this.elementType = elementType;
        this.length = length;
    }

    public Type getElementType() {
        return elementType;
    }

    public int getLength() {
        return length;
    }

    @Override
    public int getSize() {
        return elementType.getSize() * length;
    }

    @Override
    public boolean is(String s){
        return s.equals("[ " + length + " x " + elementType.toString() + " ]");
    }

    @Override
    public String toString() {
        return "[" + length + " x " + elementType.toString() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ArrayType arrayType) {
            return arrayType.elementType.equals(elementType) && arrayType.length == length;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType, length);
    }
}
