package frontend.symbols;


import java.util.LinkedList;

public class ArraySym extends Symbol {
    private int length;
    private LinkedList<Integer> initVals = new LinkedList<>();

    public ArraySym(String name, String type) {
        super(name, type);
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setInitVals(LinkedList<Integer> initVals) {
        this.initVals = initVals;
    }

    public void addInitVal(Integer initVal) {
        initVals.add(initVal);
    }

    public LinkedList<Integer> getInitVals() {
        return initVals;
    }
}
