package ir.type;

public interface Type {
    public int getSize();

    public boolean is(String s);

    @Override
    public boolean equals(Object obj);

    @Override
    public int hashCode();

    @Override
    public String toString();
}
