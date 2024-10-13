package frontend.symbols;

public class VarSym extends Symbol {
    private Object initVal = null;

    public VarSym(String name, String type) {
        super(name, type);
    }

    public void setInitVal(Object initVal) {
        this.initVal = initVal;
    }

    public Object getInitVal() {
        return initVal;
    }
}
