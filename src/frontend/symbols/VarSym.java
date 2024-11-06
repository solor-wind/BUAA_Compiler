package frontend.symbols;

public class VarSym extends Symbol {
    private int initVal = 0;

    public VarSym(String name, String type) {
        super(name, type);
    }

    public void setInitVal(int initVal) {
        this.initVal = initVal;
    }

    public int getInitVal() {
        return initVal;
    }
}
