package ir.type;

public class IntegerType implements Type{
    private int bits;

    public IntegerType(int bits) {
        this.bits = bits;
    }

    public int getBits() {
        return bits;
    }

    @Override
    public boolean is(String s){
        return s.equals("i"+bits);
    }

    @Override
    public int getSize() {
        return bits;
    }

    @Override
    public String toString() {
        return "i"+bits;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==this) return true;
        if(obj instanceof IntegerType integerType) {
            return bits == integerType.bits;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return bits;
    }
}
