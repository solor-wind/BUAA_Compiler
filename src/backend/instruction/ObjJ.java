package backend.instruction;

import backend.component.ObjInstr;
import backend.operand.ObjOperand;

public class ObjJ extends ObjInstr {
    ObjOperand operand;

    /**
     * j,jal,jr
     * b1,$ra
     */
    public ObjJ(String name, ObjOperand operand) {
        super(name);
        this.operand = operand;
    }

    public ObjOperand getOperand() {
        return operand;
    }

    public void setOperand(ObjOperand operand) {
        this.operand = operand;
    }

    @Override
    public String toString() {
        return getType() + " " + operand;
    }
}
