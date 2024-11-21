package ir.instr;

import ir.type.PointerType;
import ir.type.VoidType;
import ir.value.Instruction;
import ir.value.Value;
import ir.value.Variable;

public class GetPtrInstr extends Instruction {
    private Variable res;
    private Variable addr;
    private Value offset;

    public GetPtrInstr(Variable res, Variable addr, Value offset) {
        super("getelementptr");
        this.res = res;
        this.addr = addr;
        this.offset = offset;
    }

    public Variable getRes() {
        return res;
    }

    public Variable getAddr() {
        return addr;
    }

    public Value getOffset() {
        return offset;
    }

    public void setOffset(Value offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        if (addr.getType() instanceof PointerType ptr) {
            //TODO一般情况下，只有一层指针
            return res.getName() + " = getelementptr inbounds " + ptr.getBaseType() + ", " + addr + ", " + offset;
        } else {
            return res.getName() + " = getelementptr inbounds " + addr.getType() + ", " + addr.getType() + "* " + addr.getName() + ", i32 0, " + offset;
        }

//        String baseGap;
//        if (addr.getType() instanceof PointerType ptr) {
//            baseGap = ptr.getBaseType().toString();
//        } else {
//            baseGap = addr.getType().toString();//((ArrayType) (addr.getType())).getElementType().toString();
//        }
//        String toBaseType = "";//为了去掉type的层数
//        Type addrType = addr.getType();
//        while (!((PointerType) res.getType()).getBaseType().equals(addrType)) {
//            addrType = addr.getType() instanceof PointerType ? ((PointerType) addr.getType()).getBaseType() : ((ArrayType) (addr.getType())).getElementType();
//            toBaseType += ", i32 0";
//            break;
//        }
//        return res.getName() + " = getelementptr inbounds " + baseGap + ", " + addr + ", " + offset + toBaseType;
    }
}
