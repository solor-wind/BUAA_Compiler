package backend.process;

import backend.component.ObjBlock;
import backend.component.ObjFunction;
import backend.component.ObjInstr;
import backend.component.ObjModule;
import backend.instruction.*;
import backend.operand.*;
import backend.operand.ObjReg;


import java.util.*;

public class GraphColorAlloc {
    private ObjModule objModule;
    private HashMap<ObjReg, HashSet<ObjReg>> conflictMap = new HashMap<>();
    private HashSet<Integer> phyRegs = new HashSet<>();
    private Stack<ObjReg> stack = new Stack<>();
    private HashMap<ObjReg, Integer> spilled = new HashMap<>();
    private int spilledSize = 0;
    private ObjFunction currentFunc;
    private HashSet<ObjPhyReg> usedPhyRegs = new HashSet<>();

    public GraphColorAlloc(ObjModule objModule) {
        this.objModule = objModule;
        for (int i = 3; i < 28; i++) {
            if (i >= 8 && i <= 10) {
                continue;
            }
            phyRegs.add(i);
        }
    }

    public void run() {
        for (ObjFunction objFunction : objModule.getFunctions()) {
            this.currentFunc = objFunction;
            conflictMap = new HashMap<>();
            stack = new Stack<>();
            spilled = new HashMap<>();
            spilledSize = 0;
            usedPhyRegs = new HashSet<>();

            getBlockRelation();//梳理每个基本块之间的前后关系,合并pre,next唯一的基本块
            getBlockDefUse();//获取每个基本块的定义和使用变量
            liveRegAnalyze();//活跃变量分析，给出每个基本块的活跃变量
            genConflictGraph();//指令级的变量冲突分析
            genColorGraph();//分配节点
            regAlloc();//根据冲突图分配寄存器
            replaceReg();//将虚拟寄存器替换
        }
    }

    public void getBlockRelation() {
        //已经确保了每个基本块最后均为跳转语句
        HashSet<ObjBlock> blockSet = new HashSet<>();//当前块有没有被处理过
        ArrayList<ObjBlock> blockList = new ArrayList<>();
        for (ObjBlock objBlock : currentFunc.getBlocks().subList(1, currentFunc.getBlocks().size())) {
            if (!blockSet.contains(objBlock)) {
                blockSet.add(objBlock);
                blockList.add(objBlock);
                while (objBlock.getNextBlocks().size() == 1 && objBlock.getNextBlocks().peek().getPreBlocks().size() == 1) {
                    ObjBlock nextBlock = objBlock.getNextBlocks().peek();
                    blockSet.add(nextBlock);
                    objBlock.setNextBlocks(nextBlock.getNextBlocks());
                    LinkedList<ObjInstr> instrs = objBlock.getInstrs();
                    instrs.removeLast();
                    instrs.addAll(nextBlock.getInstrs());
                }
            }
        }
        //重新标号
        ObjBlock.blockIndex = 1;
        for (int i = 0; i < blockList.size(); i++) {
            ObjBlock block = blockList.get(i);
            block.changeName(block.getFunction().getName() + "_b" + ObjBlock.blockIndex++);
            if (i < blockList.size() - 1) {
                ObjBlock nextBlock = blockList.get(i + 1);
                ObjInstr instr = block.getInstrs().getLast();
                if (instr instanceof ObjJ j) {
                    if (j.getOperand() instanceof ObjLabel label) {
                        if (label.getBlock() != null && label.getBlock().equals(nextBlock)) {
                            block.addNextBlock(label.getBlock());
                            nextBlock.addPreBlock(block);
                            block.getInstrs().removeLast();
                        }
                    }
                }
            }
        }
        blockList.add(0, currentFunc.getBlocks().get(0));//跳过每个函数的第一个块，这个块专门用于加载参数和创建栈帧
        blockList.get(0).addNextBlock(blockList.get(1));
        blockList.get(1).addPreBlock(blockList.get(0));
        currentFunc.setBlocks(blockList);
    }

    public void getBlockDefUse() {
        for (ObjBlock objBlock : currentFunc.getBlocks()) {
            for (ObjInstr objInstr : objBlock.getInstrs()) {
                objBlock.addUses(objInstr.getRegUse());
                objBlock.addDefs(objInstr.getRegDef());
            }
        }
    }


    public void liveRegAnalyze() {
        boolean flag = true;
        ArrayList<ObjBlock> blockList = currentFunc.getBlocks();
        while (flag) {
            flag = false;
            for (int i = blockList.size() - 1; i >= 0; i--) {
                ObjBlock block = blockList.get(i);
                for (ObjBlock tmp : block.getNextBlocks()) {
                    block.liveOuts.addAll(tmp.liveIns);
                }
                HashSet<ObjReg> newIns = new HashSet<>(block.getUse());
                for (ObjReg reg : block.liveOuts) {
                    if (!block.getDef().contains(reg)) {
                        newIns.add(reg);
                    }
                }
                if (!newIns.equals(block.liveIns)) {
                    flag = true;
                    block.liveIns = newIns;
                }
            }
        }
    }

    public void genConflictGraph() {
        for (ObjBlock objBlock : currentFunc.getBlocks()) {
            boolean flag = true;
            ArrayList<ObjInstr> instrList = new ArrayList<>(objBlock.getInstrs());
            if (instrList.isEmpty()) {
                continue;
            }
            //instrList.get(0).ins.addAll(objBlock.liveIns);
            //TODO:下面这一行需要吗
            instrList.get(instrList.size() - 1).outs.addAll(objBlock.liveOuts);
            while (flag) {
                flag = false;
                for (int i = instrList.size() - 1; i >= 0; i--) {
                    ObjInstr instr = instrList.get(i);
                    if (i + 1 < instrList.size()) {
                        instr.outs.addAll(instrList.get(i + 1).ins);
                    }
                    HashSet<ObjReg> newIns = new HashSet<>(instr.getRegUse());
                    for (ObjReg reg : instr.outs) {
                        if (!instr.getRegDef().contains(reg)) {
                            newIns.add(reg);
                        }
                    }
                    if (!newIns.equals(instr.ins)) {
                        flag = true;
                        instr.ins = newIns;
                    }
                }
            }
            //构建冲突图
            for (ObjInstr instr : instrList) {
                HashSet<ObjReg> conflictSet = new HashSet<>();
                conflictSet.addAll(instr.getRegDef());
                conflictSet.addAll(instr.outs);
                for (ObjReg reg : conflictSet) {
                    if (!conflictMap.containsKey(reg)) {
                        conflictMap.put(reg, new HashSet<>());
                    }
                    conflictMap.get(reg).addAll(conflictSet);
                }
            }
        }

        for (ObjReg reg : conflictMap.keySet()) {
            conflictMap.get(reg).remove(reg);
        }
    }

    public void genColorGraph() {
        HashMap<ObjReg, HashSet<ObjReg>> tmpMap = new HashMap<>();
        for (ObjReg reg : conflictMap.keySet()) {
            if (reg instanceof ObjVirReg) {
                tmpMap.put(reg, (HashSet<ObjReg>) conflictMap.get(reg).clone());
            }
        }
        while (!tmpMap.isEmpty()) {
            boolean flag = true;
            for (ObjReg reg : tmpMap.keySet()) {
                if (tmpMap.get(reg).size() < phyRegs.size()) {
                    //改成所有的reg，冲突就冲突吧？
                    stack.push(reg);
                    for (ObjReg reg2 : tmpMap.keySet()) {
                        tmpMap.get(reg2).remove(reg);
                    }
                    tmpMap.remove(reg);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                //剩下的节点高度冲突
                ObjReg reg = null;
                for (ObjReg tmp : tmpMap.keySet()) {
                    reg = tmp;
                    break;
                }
                stack.push(reg);
                for (ObjReg reg2 : tmpMap.keySet()) {
                    tmpMap.get(reg2).remove(reg);
                }
                tmpMap.remove(reg);
            }
        }
    }

    public void regAlloc() {
        HashMap<ObjReg, HashSet<ObjReg>> tmpMap = new HashMap<>();
        for (ObjReg reg : conflictMap.keySet()) {
            tmpMap.put(reg, new HashSet<>());
            if (reg instanceof ObjPhyReg) {
                tmpMap.get(reg).addAll(conflictMap.get(reg));
            }
        }
        //先加入实际寄存器的冲突图
        for (ObjReg reg : conflictMap.keySet()) {
            if (reg instanceof ObjPhyReg) {
                for (ObjReg reg2 : conflictMap.get(reg)) {
                    tmpMap.get(reg2).add(reg);
                }
            }
        }
        while (!stack.isEmpty()) {
            ObjReg reg = stack.pop();
            HashSet<Integer> colors = (HashSet<Integer>) phyRegs.clone();
            for (ObjReg reg2 : tmpMap.get(reg)) {
                colors.remove(reg2.color);
            }
            if (!colors.isEmpty()) {
                reg.color = colors.iterator().next();
            } else {
                spilled.put(reg, spilledSize);
                spilledSize += 4;
            }
            for (ObjReg reg2 : conflictMap.get(reg)) {
                tmpMap.get(reg2).add(reg);
            }
        }
    }

    public void replaceReg() {
        for (ObjBlock objBlock : currentFunc.getBlocks()) {
            for (ObjInstr objInstr : objBlock.getInstrs()) {
                if (objInstr instanceof ObjBinary binary) {
                    binary.setDst(getAllocReg(binary.getDst()));
                    binary.setSrc1(getAllocReg(binary.getSrc1()));
                    binary.setSrc2(getAllocReg(binary.getSrc2()));
                } else if (objInstr instanceof ObjBranch branch) {
                    branch.setReg((ObjReg) getAllocReg(branch.getReg()));
                } else if (objInstr instanceof ObjJ j) {
                    j.setOperand(getAllocReg(j.getOperand()));
                } else if (objInstr instanceof ObjLoad load) {
                    load.setDst(getAllocReg(load.getDst()));
                    load.setAddr(getAllocReg(load.getAddr()));
                    load.setOffset(getAllocReg(load.getOffset()));
                } else if (objInstr instanceof ObjMove move) {
                    move.setDst(getAllocReg(move.getDst()));
                    move.setSrc(getAllocReg(move.getSrc()));
                } else if (objInstr instanceof ObjStore store) {
                    store.setPointer(getAllocReg(store.getPointer()));
                    store.setValue(getAllocReg(store.getValue()));
                    store.setOffset(getAllocReg(store.getOffset()));
                }
            }
        }
        currentFunc.setUsedPhyRegs(usedPhyRegs);
        addSpilled();
    }

    public ObjOperand getAllocReg(ObjOperand operand) {
        if (operand instanceof ObjVirReg virReg) {
            if (virReg.color != -1) {
                usedPhyRegs.add(ObjPhyReg.regs.get(virReg.color));
                return ObjPhyReg.regs.get(virReg.color);
            }
        }
        if (operand instanceof ObjPhyReg phyReg) {
            usedPhyRegs.add(phyReg);
        }
        return operand;
    }


    public void addSpilled() {
        ObjPhyReg t0 = ObjPhyReg.nameToReg.get("t0");
        ObjPhyReg t1 = ObjPhyReg.nameToReg.get("t1");
        ObjPhyReg t2 = ObjPhyReg.nameToReg.get("t2");
        ObjPhyReg sp = ObjPhyReg.nameToReg.get("sp");
        ArrayList<ObjBlock> blocks = currentFunc.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            LinkedList<ObjInstr> instrs = blocks.get(i).getInstrs();
            LinkedList<ObjInstr> newInstrs = new LinkedList<>();
            for (ObjInstr instr : instrs) {
                if (instr instanceof ObjBinary binary) {
                    if (binary.getDst().equals(ObjPhyReg.SP)) {
                        //更改一下栈帧
                        int imm = ((ObjImm) binary.getSrc2()).getImmediate();
                        if (imm < 0) {
                            binary.setSrc2(new ObjImm(imm - spilledSize));
                        } else {
                            binary.setSrc2(new ObjImm(imm + spilledSize));
                        }
                        newInstrs.add(binary);
                        continue;
                    }
                    ObjOperand dst = binary.getDst();//t0
                    ObjOperand src1 = binary.getSrc1();//t1
                    ObjOperand src2 = binary.getSrc2();//t2
                    if (dst instanceof ObjVirReg) {
                        binary.setDst(t0);
                    }
                    if (src1 instanceof ObjVirReg vreg1) {
                        newInstrs.add(new ObjLoad("lw", t1, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg1))));
                        binary.setSrc1(t1);
                    }
                    if (src2 instanceof ObjVirReg vreg2) {
                        newInstrs.add(new ObjLoad("lw", t2, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg2))));
                        binary.setSrc2(t2);
                    }
                    newInstrs.add(binary);
                    if (dst instanceof ObjVirReg vreg3) {
                        newInstrs.add(new ObjStore("sw", t0, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg3))));
                    }
                } else if (instr instanceof ObjBranch branch) {
                    if (branch.getReg() instanceof ObjVirReg vreg) {
                        newInstrs.add(new ObjLoad("lw", t0, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg))));
                        branch.setReg(t0);
                    }
                    newInstrs.add(branch);
                } else if (instr instanceof ObjJ j) {
                    if (j.getOperand() instanceof ObjVirReg vreg) {
                        newInstrs.add(new ObjLoad("lw", t0, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg))));
                        j.setOperand(t0);
                    }
                    if (j.getType().equals("jal")) {
                        ArrayList<ObjPhyReg> usedRegs = ((ObjLabel) j.getOperand()).getFunction().getUsedPhyRegs();
                        for (int k = 0; k < usedRegs.size(); k++) {
                            if (usedRegs.get(k).equals(ObjPhyReg.RA) || usedRegs.get(k).equals(ObjPhyReg.SP) || usedRegs.get(k).equals(ObjPhyReg.nameToReg.get("v0"))) {
                                continue;
                            }
                            newInstrs.add(new ObjStore("sw", usedRegs.get(k), ObjPhyReg.SP, new ObjImm(currentFunc.argSize + k * 4)));
                        }
                    }
                    newInstrs.add(j);
                    if (j.getType().equals("jal")) {
                        ArrayList<ObjPhyReg> usedRegs = ((ObjLabel) j.getOperand()).getFunction().getUsedPhyRegs();
                        for (int k = 0; k < usedRegs.size(); k++) {
                            if (usedRegs.get(k).equals(ObjPhyReg.RA) || usedRegs.get(k).equals(ObjPhyReg.SP) || usedRegs.get(k).equals(ObjPhyReg.nameToReg.get("v0"))) {
                                continue;
                            }
                            newInstrs.add(new ObjStore("lw", usedRegs.get(k), ObjPhyReg.SP, new ObjImm(currentFunc.argSize + k * 4)));
                        }
                    }
                } else if (instr instanceof ObjLoad load) {
                    if (blocks.get(i).equals(blocks.get(0))) {
                        //传递参数时，要获取上一个函数的栈帧中的参数
                        ObjVirReg virReg = null;
                        if (load.getDst() instanceof ObjVirReg) {
                            virReg = (ObjVirReg) load.getDst();
                            load.setDst(t0);
                        }
                        if (load.getAddr() instanceof ObjVirReg vreg1) {
                            load.setAddr(t1);
                            newInstrs.add(new ObjLoad("lw", t1, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg1))));
                        }
                        if (load.getOffset() instanceof ObjVirReg vreg2) {
                            load.setOffset(t2);
                            newInstrs.add(new ObjLoad("lw", t2, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg2))));
                        } else if (load.getOffset() instanceof ObjImm imm) {
                            load.setOffset(new ObjImm(imm.getImmediate() + spilledSize));//更改了load参数时的偏移
                        }
                        newInstrs.add(load);
                        if (virReg != null) {
                            newInstrs.add(new ObjStore("sw", t0, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(virReg))));
                        }
                        continue;
                    }

                    ObjVirReg virReg = null;
                    if (load.getDst() instanceof ObjVirReg) {
                        virReg = (ObjVirReg) load.getDst();
                        load.setDst(t0);
                    }
                    if (load.getAddr() instanceof ObjVirReg vreg1) {
                        load.setAddr(t1);
                        newInstrs.add(new ObjLoad("lw", t1, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg1))));
                    }
                    if (load.getOffset() instanceof ObjVirReg vreg2) {
                        load.setOffset(t2);
                        newInstrs.add(new ObjLoad("lw", t2, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg2))));
                    }
                    newInstrs.add(load);
                    if (virReg != null) {
                        newInstrs.add(new ObjStore("sw", t0, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(virReg))));
                    }
                } else if (instr instanceof ObjMove move) {
                    ObjVirReg vreg1 = null;
                    if (move.getDst() instanceof ObjVirReg) {
                        vreg1 = (ObjVirReg) move.getDst();
                        move.setDst(t0);
                    }
                    if (move.getSrc() instanceof ObjVirReg vreg2) {
                        move.setSrc(t1);
                        newInstrs.add(new ObjLoad("lw", t1, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg2))));
                    }
                    newInstrs.add(move);
                    if (vreg1 != null) {
                        newInstrs.add(new ObjStore("sw", t0, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg1))));
                    }
                } else if (instr instanceof ObjStore store) {
                    if (store.getValue() instanceof ObjVirReg vreg) {
                        store.setValue(t0);
                        newInstrs.add(new ObjLoad("lw", t0, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg))));
                    }
                    if (store.getPointer() instanceof ObjVirReg vreg1) {
                        store.setPointer(t1);
                        newInstrs.add(new ObjLoad("lw", t1, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg1))));
                    }
                    if (store.getOffset() instanceof ObjVirReg vreg2) {
                        store.setOffset(t2);
                        newInstrs.add(new ObjLoad("lw", t2, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg2))));
                    }
                    newInstrs.add(store);
                } else {
                    newInstrs.add(instr);
                }
            }
            blocks.get(i).resetInstrs(newInstrs);
        }
    }
}
