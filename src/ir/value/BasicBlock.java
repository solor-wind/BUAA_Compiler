package ir.value;

import ir.type.OtherType;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private Function function;//所属的函数
    private BasicBlock preBlock;//前一个基本快
    private BasicBlock nextBlock;//后一个基本块

    private ArrayList<Instruction> instructions = new ArrayList<>();

    public BasicBlock(String name, Function function) {
        super(function.getName().substring(1) + "_" + name, new OtherType("basicblock"));
        this.function = function;
    }

    public void setPreBlock(BasicBlock preBlock) {
        this.preBlock = preBlock;
    }

    public void setNextBlock(BasicBlock nextBlock) {
        this.nextBlock = nextBlock;
    }

    public BasicBlock getPreBlock() {
        return preBlock;
    }

    public BasicBlock getNextBlock() {
        return nextBlock;
    }

    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
    }

    public void addFirstInstruction(Instruction instruction) {
        instructions.add(0, instruction);
    }

    public void setInstructions(ArrayList<Instruction> instructions) {
        this.instructions = instructions;
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName() + ":\n");
        for (Instruction instruction : instructions) {
            sb.append("\t" + instruction.toString() + "\n");
        }
        return sb.toString();
    }
}
