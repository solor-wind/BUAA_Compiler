package ir.value;

import ir.type.Type;

public class Variable extends Value {

    private boolean isConstant = false;
    private boolean isGlobal = false;
    private boolean isArray = false;
    private Object initValue;//int,ArrayList<Integer>
    private String stringConst = null;

    public Variable(String name, Type type) {
        super(name, type);
    }

    public Variable(String name, Type type, boolean isConstant, boolean isGlobal) {
        super(name, type);
        this.isConstant = isConstant;
        this.isGlobal = isGlobal;
    }

    public void setGlobal(boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setArray(boolean isArray) {
        this.isArray = isArray;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setConstant(boolean constant) {
        isConstant = constant;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public Object getInitValue() {
        return initValue;
    }

    public void setInitValue(Object initValue) {
        this.initValue = initValue;
    }

    public String getStringConst() {
        return stringConst;
    }

    public void setStringConst(String stringConst) {
        this.stringConst = stringConst;
    }
}
