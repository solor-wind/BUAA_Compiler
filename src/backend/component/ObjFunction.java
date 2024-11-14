package backend.component;

import ir.value.Argument;

import java.util.ArrayList;

public class ObjFunction {
    private String name;
    private ArrayList<ObjBlock> blocks = new ArrayList<>();
    private ArrayList<Argument> args = new ArrayList<>();
    private int stackSize = 0;
    public int allocaSize = 0;
    public int regSize = 32;
    public int argSize = 0;
    private int allocas = 0;

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

    public int getStackSize() {
        return allocaSize + regSize + argSize + 4;
    }

    public int getNextAlloca() {
        return argSize + regSize + 4 + allocas;
    }

    public int getNextAlloca(int i) {
        int ans = argSize + regSize + 4 + allocas;
        allocas += i;
        return ans;
    }

    public void addAllocas(int i) {
        allocas += i;
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