package frontend.symbols;

public class Symbol {
    private String name;
    private String type;
    private boolean isGlobal;

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    /**
     * 返回是否是某类型或其子类型
     * Const,Array,Func,Int,Char
     */
    public boolean is(String type) {
        return this.type.contains(type);
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }
}
