package ir.instr;

import ir.type.Type;
import ir.value.Instruction;
import ir.value.Value;

public class TruncInstr extends Instruction {
    private Value res;
    private Value val;
    private Type fromType;
    private Type toType;

    public TruncInstr(Value res, Value val, Type fromType, Type toType) {
        super("trunc");
        this.res = res;
        this.val = val;
        this.fromType = fromType;
        this.toType = toType;
        uses.add(val);
        defs.add(res);
    }

    public TruncInstr(Value res, Value val) {
        super("trunc");
        this.res = res;
        this.val = val;
        this.fromType = val.getType();
        this.toType = res.getType();
        uses.add(val);
        defs.add(res);
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
        return res.getName() + " = trunc " + fromType + " " + val.getName() + " to " + toType;
    }
}
