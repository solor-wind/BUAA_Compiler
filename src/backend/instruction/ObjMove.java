package backend.instruction;

import backend.component.ObjInstr;
import backend.operand.*;

public class ObjMove extends ObjInstr {
    //move,li
    private ObjOperand src;
    private ObjOperand dst;
    public boolean needchange = false;

    public ObjMove(String type, ObjOperand dst, ObjOperand src) {
        super(type);
        this.src = src;
        this.dst = dst;
        addDef(this.dst);
        addUse(this.src);
    }

    public ObjOperand getSrc() {
        return src;
    }

    public ObjOperand getDst() {
        return dst;
    }

    public void setDst(ObjOperand dst) {
        addDefReg(this.dst, dst);
        this.dst = dst;
    }

    public void setSrc(ObjOperand src) {
        addUseReg(this.src, src);
        this.src = src;
    }

    @Override
    public String toString() {
        return getType() + "\t" + getDst() + ",\t" + getSrc();
    }

    @Override
    public void replaceReg(ObjOperand oldReg, ObjOperand newReg) {
        if (dst.equals(oldReg)) {
            setDst(newReg);
        }
        if (src.equals(oldReg)) {
            setSrc(newReg);
        }
    }

    public void appendOffsetIfMarked(int append) {
        if (this.getType().equals("li.stack")) {
            this.src = new ObjImm(append + ((ObjImm) src).getImmediate());
        }
        this.setType("li");
    }
}
