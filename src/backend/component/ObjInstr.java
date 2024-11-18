package backend.component;

import backend.operand.ObjOperand;
import backend.operand.ObjReg;

import java.util.HashSet;

public class ObjInstr {
    private String type;
    public HashSet<ObjReg> regDef, regUse;
    public HashSet<ObjReg> ins = new HashSet<>();
    public HashSet<ObjReg> outs = new HashSet<>();
    public int index = -1;

    public ObjInstr(String type) {
        this.type = type;
        this.regDef = new HashSet<>();
        this.regUse = new HashSet<>();
    }

    public String getType() {
        return type;
    }

    public void changeType(String type) {
        this.type = type;
    }

    public HashSet<ObjReg> getRegUse() {
        return regUse;
    }

    public void addUse(ObjOperand operand) {
        if (operand instanceof ObjReg reg) {
            regUse.add(reg);
            regDef.remove(reg);
        }
    }

    public HashSet<ObjReg> getRegDef() {
        return regDef;
    }

    public void addDef(ObjOperand operand) {
        if (operand instanceof ObjReg reg) {
            regDef.add(reg);
        }
    }

    private void removeDef(ObjOperand reg) {
        if (reg instanceof ObjReg) {
            regDef.remove((ObjReg) reg);
        }
    }

    private void removeUse(ObjOperand reg) {
        if (reg instanceof ObjReg) {
            regUse.remove((ObjReg) reg);
        }
    }

    public void addDefReg(ObjOperand oldReg, ObjOperand newReg) {
        if (oldReg != null) {
            removeDef(oldReg);
        }
        addDef(newReg);
    }

    public void addUseReg(ObjOperand oldReg, ObjOperand newReg) {
        if (oldReg != null) {
            removeUse(oldReg);
        }
        addUse(newReg);
    }

    @Override
    public String toString() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void replaceReg(ObjOperand oldReg, ObjOperand newReg) {
        throw new RuntimeException("replaceReg not implemented");
    }
}
