package ir.value;

import ir.IRBuilder;
import ir.instr.BrInstr;
import ir.instr.LabelInstr;
import ir.instr.RetInstr;
import ir.type.Type;

import java.util.*;

public class Function extends Value {
    private ArrayList<Argument> arguments = new ArrayList<>();
    private ArrayList<BasicBlock> blocks = new ArrayList<>();
    private HashMap<String, Value> variables = new HashMap<>();//语义分析符号表到函数内符号表达的映射

    private HashMap<String, String> tmpMap = new HashMap<>();//从符号表到原符号表的映射
    private HashMap<String, BasicBlock> blockMap = new HashMap<>();//解决分支、循环中的插入问题

    public Function(String name, Type type) {
        super(name, type);
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    public void addArgument(Argument argument) {
        arguments.add(argument);
    }

    public void setArguments(ArrayList<Argument> arguments) {
        this.arguments = arguments;
    }

    public ArrayList<BasicBlock> getBlocks() {
        return blocks;
    }

    public void addBlock(BasicBlock block) {
        blocks.add(block);
        blockMap.put(block.getName(), block);
    }

    public void addBlocks(LinkedList<BasicBlock> blocks) {
        for (BasicBlock block : blocks) {
            blockMap.put(block.getName(), block);
        }
    }

    public HashMap<String, BasicBlock> getBlockMap() {
        return blockMap;
    }

    /**
     * 查找block中含有分支、跳转的语句，并分为多个基本块、插入分支块
     */
    public void divideBlock(BasicBlock block) {
        ArrayList<Instruction> instrs = block.getInstructions();
        ArrayList<BasicBlock> blocks = new ArrayList<>();
        blocks.add(block);
        int startIndex = 0;
        for (int i = 0; i < instrs.size(); i++) {
            if (instrs.get(i) instanceof BrInstr brInstr
                    && i + 1 < instrs.size() &&
                    instrs.get(i + 1) instanceof LabelInstr labelInstr) {
                //旧的块划清界限，并连接后续的分支判断
                block.setInstructions(new ArrayList<>(instrs.subList(startIndex, i + 1)));
                BasicBlock tmp = labelInstr.getLabel();
                if (block.getNextBlock() != null) {
                    //对应if里的stmt中嵌套if的情况
                    IRBuilder.connectBlock(tmp, block.getNextBlock());
                }
                IRBuilder.connectBlock(block, brInstr.getBlock1());
                //新的块
                block = tmp;
                blocks.add(block);
                startIndex = i + 2;
            }
        }

        for (int i = 0; i < blocks.size() - 1; i++) {
            BasicBlock tmpblock = blocks.get(i);
            while (tmpblock.getNextBlock() != null) {
                tmpblock = tmpblock.getNextBlock();
                divideBlock(tmpblock);
            }
            IRBuilder.connectBlock(tmpblock, blocks.get(i + 1));
        }
        block.setInstructions(new ArrayList<>(instrs.subList(startIndex, instrs.size())));
    }

    public void sortBlock() {
        for (BasicBlock block : blocks) {
            divideBlock(block);
        }

        //连接
        ArrayList<BasicBlock> newblocks = new ArrayList<>();
        for (BasicBlock block : blocks) {
            if (!newblocks.isEmpty()) {
                IRBuilder.connectBlock(newblocks.get(newblocks.size() - 1), block);
            }
            newblocks.add(block);
            BasicBlock tmpblock = block.getNextBlock();
            while (tmpblock != null) {
                newblocks.add(tmpblock);
                tmpblock = tmpblock.getNextBlock();
            }
        }

        //在空块中加入跳转语句，去掉多余跳转语句，补充返回语句
        for (int i = 0; i < newblocks.size() - 1; i++) {
            if (newblocks.get(i).getInstructions().isEmpty()) {
                newblocks.get(i).addInstruction(new BrInstr(newblocks.get(i + 1)));
            } else {
                ArrayList<Instruction> tmpInstrs = newblocks.get(i).getInstructions();
                boolean tmFlag = false;
                for (int j = 0; j < tmpInstrs.size(); j++) {
                    if (tmpInstrs.get(j) instanceof BrInstr) {
                        tmpInstrs = new ArrayList<>(tmpInstrs.subList(0, j + 1));
                        tmFlag = true;
                        break;
                    }
                }
                if (!tmFlag && (tmpInstrs.isEmpty() || !(tmpInstrs.get(tmpInstrs.size() - 1) instanceof RetInstr))) {
                    tmpInstrs.add(new RetInstr(null));
                }
                newblocks.get(i).setInstructions(tmpInstrs);
            }
        }

        ArrayList<Instruction> tmpInstrs = newblocks.get(newblocks.size() - 1).getInstructions();
        boolean tmFlag = false;
        for (int j = 0; j < tmpInstrs.size(); j++) {
            if (tmpInstrs.get(j) instanceof BrInstr) {
                tmpInstrs = new ArrayList<>(tmpInstrs.subList(0, j + 1));
                tmFlag = true;
                break;
            }
        }
        if (!tmFlag && (tmpInstrs.isEmpty() || !(tmpInstrs.get(tmpInstrs.size() - 1) instanceof RetInstr))) {
            tmpInstrs.add(new RetInstr(null));
        }
        newblocks.get(newblocks.size() - 1).setInstructions(tmpInstrs);

        this.blocks = newblocks;
    }

    public void addVariable(String key, Value value) {
        variables.put(key, value);
        tmpMap.put(value.getName(), key);
    }

    public Value getVariable(String key) {
        if (variables.containsKey(key)) {
            return variables.get(key);
        } else {
            return IRBuilder.irModule.getGlobalVariable(key.substring(1));
        }
    }

    public void changeMap(Value oldValue, Value newValue) {
        variables.put(tmpMap.get(oldValue.getName()), newValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("define " + getType() + " " + getName());
        sb.append("(");
        if (!arguments.isEmpty()) {
            sb.append(arguments.get(0).toString());
            for (int i = 1; i < arguments.size(); i++) {
                sb.append(", ").append(arguments.get(i).toString());
            }
        }
        sb.append(") {\n");
        for (BasicBlock b : blocks) {
            sb.append(b.toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
