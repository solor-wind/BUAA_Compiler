package ir.instr;

import ir.value.Instruction;
import ir.value.Value;

public class BinaInstr extends Instruction {
    private Value val1;
    private Value val2;
    private Value res;

    /**
     * add,sub,mul,sdiv,srem
     */
    public BinaInstr(String typeName, Value res, Value val1, Value val2) {
        super(typeName);
        this.val1 = val1;
        this.val2 = val2;
        this.res = res;
    }

    @Override
    public String toString() {
        return res.getName() + " = " + getName() + " " + val1.getType() + " " + val1.getName() + ", " + val2.getName();
    }

    public Value getVal1() {
        return val1;
    }

    public Value getVal2() {
        return val2;
    }

    public Value getRes() {
        return res;
    }
}
