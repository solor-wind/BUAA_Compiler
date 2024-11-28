package backend.component;

import backend.operand.ObjPhyReg;
import ir.value.Argument;

import java.util.ArrayList;
import java.util.HashSet;

public class ObjFunction {
    private String name;
    private ArrayList<ObjBlock> blocks = new ArrayList<>();
    private ArrayList<Argument> args = new ArrayList<>();
    private int stackSize = 0;
    public int allocaSize = 0;
    public int regSize = 128;
    public int argSize = 0;
    private int allocas = 0;
    private ArrayList<ObjPhyReg> usedPhyRegs = new ArrayList<>();

    public ObjFunction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addBlock(ObjBlock block) {
        blocks.add(block);
    }

    public ArrayList<ObjBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(ArrayList<ObjBlock> blocks) {
        this.blocks = blocks;
    }

    public int getStackSize() {
        return allocaSize + regSize + argSize + 4;
    }

    public int getNextAlloca(int i) {
        int ans = argSize + regSize + 4 + allocas;
        allocas += i;
        return ans;
    }

    public ArrayList<ObjPhyReg> getUsedPhyRegs() {
        return usedPhyRegs;
    }

    public void setUsedPhyRegs(HashSet<ObjPhyReg> usedPhyRegs) {
        this.usedPhyRegs.clear();
        this.usedPhyRegs.addAll(usedPhyRegs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName()).append(":\n");
        for (ObjBlock block : blocks) {
            sb.append(block.toString()).append("\n");
        }
        return sb.toString();
    }

}