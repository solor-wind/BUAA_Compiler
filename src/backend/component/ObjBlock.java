package backend.component;

import backend.operand.ObjReg;
import backend.operand.ObjVirReg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;

public class ObjBlock {
    private String name;
    private LinkedList<ObjInstr> instrs;
    private LinkedList<ObjBlock> preBlocks = new LinkedList<>();//能跳转至这段代码块的代码块
    private LinkedList<ObjBlock> nextBlocks = new LinkedList<>();//这段代码块能跳转至哪些代码块
    public static int blockIndex = 0;
    private int index;
    private ObjFunction function;//所属的函数
    private boolean isBrDest;
    public final HashSet<ObjReg> Use = new HashSet<>();
    public final HashSet<ObjReg> Def = new HashSet<>();
    public HashSet<ObjReg> liveIns = new HashSet<>();
    public HashSet<ObjReg> liveOuts = new HashSet<>();
    public final ArrayList<ArrayList<ObjReg>> LocalInterfere = new ArrayList<>();
    public int depth;

    public ObjBlock(ObjFunction function) {
        index = blockIndex++;
        this.name = function.getName() + "_b" + index;
        instrs = new LinkedList<>();
        preBlocks = new LinkedList<>();
        nextBlocks = new LinkedList<>();
        this.function = function;
        isBrDest = false;
    }

    public void resetInstrs(LinkedList<ObjInstr> instrs) {
        this.instrs = instrs;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public ObjFunction getFunction() {
        return function;
    }

    public ObjBlock(String name, ObjFunction function) {
        this.name = name;
        instrs = new LinkedList<>();
        preBlocks = new LinkedList<>();
        nextBlocks = new LinkedList<>();
        this.function = function;
        isBrDest = false;
    }

    public ObjBlock(String name) {
        this.name = name;
        isBrDest = true;
    }

    public LinkedList<ObjInstr> getInstrs() {
        if (isBrDest) {
            return null;
        }
        return instrs;
    }

    public void addInstr(ObjInstr objInstr) {
        if (isBrDest) {
            return;
        }
        instrs.addLast(objInstr);
    }

    public void addUses(HashSet<ObjReg> regs) {
        Use.addAll(regs);
    }

    public HashSet<ObjReg> getUse() {
        return Use;
    }

    public void addDefs(HashSet<ObjReg> regs) {
        for (ObjReg reg : regs) {
            if (!Use.contains(reg)) {
                Def.add(reg);//在任何使用前，被定义
            }
        }
    }

    public HashSet<ObjReg> getDef() {
        return Def;
    }

    public void addFirstInstr(ObjInstr objInstr) {
        instrs.addFirst(objInstr);
    }

    public void addPreBlock(ObjBlock block) {
//        if (isBrDest) {
//            return;
//        }
        preBlocks.addLast(block);
    }

    public void setPreBlocks(LinkedList<ObjBlock> preBlocks) {
        this.preBlocks = preBlocks;
    }

    public void removePreBlock(ObjBlock block) {
        if (isBrDest) {
            return;
        }
        preBlocks.remove(block);
    }

    public void addNextBlock(ObjBlock block) {
//        if (isBrDest) {
//            return;
//        }
        nextBlocks.addLast(block);
    }

    public void setNextBlocks(LinkedList<ObjBlock> nextBlocks) {
        this.nextBlocks = nextBlocks;
    }

    public void removeNextBlock(ObjBlock block) {
        if (isBrDest) {
            return;
        }
        nextBlocks.remove(block);
    }

    public LinkedList<ObjBlock> getPreBlocks() {
//        if (isBrDest) {
//            return null;
//        }
        return preBlocks;
    }

    public LinkedList<ObjBlock> getNextBlocks() {
//        if (isBrDest) {
//            return null;
//        }
        return nextBlocks;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (isBrDest) {
            return getName() + "is dest of a branch instr.";
        }
        StringBuilder string = new StringBuilder(getName() + ":\n");
        for (ObjInstr instr : instrs) {
            string.append("\t" + instr.toString() + "\n");
        }
        return string.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ObjBlock)) {
            return false;
        } else if (isBrDest || ((ObjBlock) obj).checkIfBrDest()) {
            return ((ObjBlock) obj).name.equals(this.name);
        }
        return ((ObjBlock) obj).name.equals(this.name) && ((ObjBlock) obj).function.getName().equals(this.function.getName());
    }

    @Override
    public int hashCode() {
        if (isBrDest) {
            return -1;
        }
        return Objects.hash(name, function);
    }

    public boolean checkIfBrDest() {
        return isBrDest;
    }
}
