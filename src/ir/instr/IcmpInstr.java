package ir.instr;

import ir.value.Instruction;
import ir.value.Value;

public class IcmpInstr extends Instruction {
    private Value lv;
    private Value rv;
    private Value res;
    private String op;

    /**
     * op:eq,ne,sgt,sge,slt,sle
     */
    public IcmpInstr(Value res, Value lv, Value rv, String op) {
        super("icmp");
        this.lv = lv;
        this.rv = rv;
        this.res = res;
        this.op = op;
    }

    public Value getLv() {
        return lv;
    }

    public void setLv(Value lv) {
        this.lv = lv;
    }

    public Value getRv() {
        return rv;
    }

    public void setRv(Value rv) {
        this.rv = rv;
    }

    public Value getRes() {
        return res;
    }

    public String getOp() {
        return op;
    }

    @Override
    public String toString() {
        return res.getName() + " = icmp " + op + " " + lv + ", " + rv.getName();
    }
}
