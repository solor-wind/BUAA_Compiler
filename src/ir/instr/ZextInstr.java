package ir.instr;

import ir.type.Type;
import ir.value.Instruction;
import ir.value.Value;

public class ZextInstr extends Instruction {
    private Value res;
    private Value val;
    private Type fromType;
    private Type toType;

    public ZextInstr(Value res, Value val, Type fromType, Type toType) {
        super("zext");
        this.res = res;
        this.val = val;
        this.fromType = fromType;
        this.toType = toType;
    }

    public ZextInstr(Value res, Value val) {
        super("zext");
        this.res = res;
        this.val = val;
        this.fromType = val.getType();
        this.toType = res.getType();
        defs.add(res);
        uses.add(val);
    }

    public Value getRes() {
        return res;
    }

    public Value getVal() {
        return val;
    }

    public void setVal(Value val) {
        uses.remove(this.val);
        uses.add(val);
        this.val = val;
    }

    @Override
    public String toString() {
        return res.getName() + " = zext " + fromType + " " + val.getName() + " to " + toType;
    }
}
