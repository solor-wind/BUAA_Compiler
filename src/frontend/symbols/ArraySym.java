package frontend.symbols;


import java.util.LinkedList;

public class ArraySym extends Symbol {
    private int length;
    private LinkedList<Object> initVals;//可能为Integer,Character,Exp

    public ArraySym(String name, String type) {
        super(name, type);
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setInitVals(LinkedList<Object> initVals) {
        this.initVals = initVals;
    }

    public void addInitVal(Object initVal) {
        initVals.add(initVal);
    }

    public LinkedList<Object> getInitVals() {
        return initVals;
    }
}
