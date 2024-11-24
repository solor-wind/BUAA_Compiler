package midOPT;

import ir.IRBuilder;
import ir.instr.BrInstr;
import ir.instr.PCInstr;
import ir.instr.PhiInstr;
import ir.value.*;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RemovePhi {
    private IRModule irModule;
    private CFG cfg;
    private Function currentFuction;
    private HashSet<BasicBlock> toInsertBlocks = new HashSet<>();

    public RemovePhi(IRModule irModule, CFG cfg) {
        this.irModule = irModule;
        this.cfg = cfg;
    }

    public void run() {
        for (Function function : irModule.functions) {
            currentFuction = function;
            toInsertBlocks.clear();
            insertPC();//插入PC（并行赋值块）
            insertBlock();//将新建的Block加入函数
            PCToMove();
        }
    }

    public void insertPC() {
        IRBuilder.blockName = currentFuction.getBlocks().size();
        for (BasicBlock block : currentFuction.getBlocks()) {
            HashMap<BasicBlock, BasicBlock> newBlockMap = new HashMap<>();//关键边的来源-新的块
            ArrayList<PhiInstr> phiInstrs = new ArrayList<>();
            for (Instruction instr : block.getInstructions()) {
                if (instr instanceof PhiInstr phi) {
                    phiInstrs.add(phi);
                } else {
                    break;
                }
            }
            for (PhiInstr phi : phiInstrs) {
                Value res = phi.getRes();
                for (Pair<Value, BasicBlock> pair : phi.getValLabel()) {
                    if (cfg.nexts.get(pair.getSecond()).size() > 1) {
                        //可能跳转至其他地方，是关键边
                        BasicBlock newBlock;
                        if (newBlockMap.containsKey(pair.getSecond())) {
                            newBlock = newBlockMap.get(pair.getSecond());
                        } else {
                            newBlock = new BasicBlock(IRBuilder.getBlockName(currentFuction), currentFuction);
                            toInsertBlocks.add(newBlock);
                            newBlockMap.put(pair.getSecond(), newBlock);
                            newBlock.addInstruction(new PCInstr());
                            newBlock.addInstruction(new BrInstr(block));
                            //改变原来块的br指令，跳转至新块
                            BrInstr br = (BrInstr) pair.getSecond().getInstructions().get(pair.getSecond().getInstructions().size() - 1);
                            if (br.getBlock1().equals(block)) {
                                br.setBlock1(newBlock);
                            } else if (br.getBlock2().equals(block)) {
                                br.setBlock2(newBlock);
                            }
                            //改变CFG
                            cfg.nexts.put(newBlock, new HashSet<>());
                            cfg.nexts.get(newBlock).add(block);
                            cfg.prevs.put(newBlock, new HashSet<>());
                            cfg.prevs.get(newBlock).add(pair.getSecond());
                            cfg.prevs.get(block).remove(pair.getSecond());
                            cfg.prevs.get(block).add(newBlock);
                            //pair.getSecond()的部分后面再改
                        }
                        PCInstr pc = (PCInstr) newBlock.getInstructions().get(0);
                        pc.addMove(res, pair.getFirst());
                    } else {
                        //在跳转之前插入PC即可
                        //TODO:基本块自己跳自己
                        ArrayList<Instruction> instrs = pair.getSecond().getInstructions();
                        PCInstr pc = new PCInstr();
                        if (instrs.size() < 2) {
                            instrs.add(0, pc);
                        } else if (!(instrs.get(instrs.size() - 2) instanceof PCInstr)) {
                            instrs.add(instrs.size() - 1, pc);
                        } else {
                            pc = (PCInstr) instrs.get(instrs.size() - 2);
                        }
                        pc.addMove(res, pair.getFirst());
                    }
                }
            }
            //修改nexts和prevs
            for (BasicBlock b : newBlockMap.keySet()) {
                cfg.nexts.get(b).add(newBlockMap.get(b));
                cfg.nexts.get(b).remove(cfg.nexts.get(newBlockMap.get(b)).iterator().next());
            }
            while (block.getInstructions().get(0) instanceof PhiInstr) {
                block.getInstructions().remove(0);
            }
        }
    }

    public void insertBlock() {
        ArrayList<BasicBlock> newblocks = new ArrayList<>();
        for (BasicBlock block : currentFuction.getBlocks()) {
            newblocks.add(block);
            for (BasicBlock b : cfg.nexts.get(block)) {
                if (toInsertBlocks.contains(b)) {
                    toInsertBlocks.remove(b);
                    newblocks.add(b);
                    break;
                }
            }
        }
        currentFuction.setBlocks(newblocks);
    }

    public void PCToMove() {

    }
}
