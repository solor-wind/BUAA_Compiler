package ir.type;

public class VoidType implements Type{

    @Override
    public boolean is(String s){
        return s.equals("void");
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public String toString() {
        return "void";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoidType;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
