package ir.type;

import java.util.Objects;

public class OtherType implements Type {

    String name;

    /**
     * function,basicblock,instruction
     */
    public OtherType(String name) {
        this.name = name;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public boolean is(String s) {
        return s.equals(name);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OtherType && ((OtherType) obj).name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
