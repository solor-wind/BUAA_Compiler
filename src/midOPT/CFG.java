package midOPT;

import ir.IRBuilder;
import ir.instr.BrInstr;
import ir.value.BasicBlock;
import ir.value.Function;
import ir.value.IRModule;
import ir.value.Instruction;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CFG {
    //生成控制流程图
    public HashMap<BasicBlock, HashSet<BasicBlock>> prevs = new HashMap<>();
    public HashMap<BasicBlock, HashSet<BasicBlock>> nexts = new HashMap<>();
    public HashSet<Pair<BasicBlock, BasicBlock>> edges = new HashSet<>();
    private IRModule irModule;

    public CFG(IRModule irModule) {
        this.irModule = irModule;
    }

    public void run() {
        prevs.clear();
        nexts.clear();
        for (Function function : irModule.functions) {
            for (BasicBlock block : function.getBlocks()) {
                prevs.put(block, new HashSet<>());
                nexts.put(block, new HashSet<>());
            }
        }

        for (Function function : irModule.functions) {
            for (BasicBlock block : function.getBlocks()) {
                Instruction instruction = block.getInstructions().get(block.getInstructions().size() - 1);
                if (instruction instanceof BrInstr br) {
                    nexts.get(block).add(br.getBlock1());
                    prevs.get(br.getBlock1()).add(block);
                    if (br.getBlock2() != null) {
                        nexts.get(block).add(br.getBlock2());
                        prevs.get(br.getBlock2()).add(block);
                    }
                }
            }
        }
    }

    public void calEges() {
        for (BasicBlock block : prevs.keySet()) {
            for (BasicBlock block2 : prevs.get(block)) {
                edges.add(new Pair<>(block2, block));
            }
        }
        for (BasicBlock block : nexts.keySet()) {
            for (BasicBlock block2 : nexts.get(block)) {
                edges.add(new Pair<>(block, block2));
            }
        }
    }

    public void deleteBlock() {
        //删除无法到达的块
        for (Function function : irModule.functions) {
            ArrayList<BasicBlock> blocks = new ArrayList<>();
            HashSet<BasicBlock> visited = new HashSet<>();
            dfs(function.getBlocks().get(0), visited);
            for (BasicBlock block : function.getBlocks()) {
                if (visited.contains(block)) {
                    blocks.add(block);
                }
            }
            function.setBlocks(blocks);
        }
    }

    public void dfs(BasicBlock block, HashSet<BasicBlock> visited) {
        visited.add(block);
        for (BasicBlock block2 : nexts.get(block)) {
            if (!visited.contains(block2)) {
                dfs(block2, visited);
            }
        }
    }

    public void mergeBlock() {
        //合并连续跳转的块
        for (Function function : irModule.functions) {
            HashSet<BasicBlock> solved = new HashSet<>();
            ArrayList<BasicBlock> blocks = new ArrayList<>();
            blocks.add(function.getBlocks().get(0));
            function.getBlocks().remove(0);
            //TODO:对于只有一条跳转语句的基本块，还可以合并
            for (BasicBlock block : function.getBlocks()) {
                if (!solved.contains(block)) {
                    solved.add(block);
                    blocks.add(block);
                    while (nexts.get(block).size() == 1 && prevs.get(nexts.get(block).iterator().next()).size() == 1) {
                        BasicBlock nextBlock = nexts.get(block).iterator().next();
                        solved.add(nextBlock);
                        block.getInstructions().remove(block.getInstructions().size() - 1);
                        block.getInstructions().addAll(nextBlock.getInstructions());
                        nexts.put(block, nexts.get(nextBlock));
                    }
                }
            }
            IRBuilder.blockName = 0;
            for (BasicBlock block : blocks) {
                block.changeName(function.getName().substring(1) + "_" + IRBuilder.getBlockName());
            }
            function.setBlocks(blocks);
        }
    }

}
