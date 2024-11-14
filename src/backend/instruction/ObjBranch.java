package backend.instruction;

import backend.component.ObjInstr;
import backend.operand.ObjLabel;
import backend.operand.ObjReg;

public class ObjBranch extends ObjInstr {
    private ObjReg reg;
    private ObjLabel label;

    public ObjBranch(String type, ObjReg reg, ObjLabel label) {
        super(type);
        this.reg = reg;
        this.label = label;
    }

    public ObjReg getReg() {
        return reg;
    }

    public void setReg(ObjReg reg) {
        this.reg = reg;
    }

    @Override
    public String toString() {
        return getType() + " " + reg + " " + label;
    }
}
