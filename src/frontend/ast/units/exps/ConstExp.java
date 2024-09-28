package frontend.ast.units.exps;

public class ConstExp {
    private AddExp addExp;
    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
    }
    @Override
    public String toString() {
        return addExp+"\n<ConstExp>";
    }
}
