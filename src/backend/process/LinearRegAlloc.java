package backend.process;

import backend.component.*;
import backend.instruction.*;
import backend.operand.*;
import utils.Pair;

import java.util.*;

public class LinearRegAlloc {
    private ObjModule objModule;
    private ObjFunction currentFunc;
    private ArrayList<ObjInstr> instrs = new ArrayList<>();
    private ArrayList<ObjBlock> blocks = new ArrayList<>();
    private HashMap<ObjReg, Pair<Integer, Integer>> activeRange = new HashMap<>();
    private HashMap<ObjVirReg, ObjPhyReg> allocated = new HashMap<>();//给哪个虚拟寄存器分配了哪个实际寄存器
    //private LinkedList<ObjPhyReg> freeRegs = new LinkedList<>();
    private HashMap<ObjPhyReg, LinkedList<Pair<Integer, Integer>>> phyRegRange = new HashMap<>();//每个物理寄存器在哪些地方被占用
    private HashMap<ObjVirReg, Integer> spilled = new HashMap<>();//溢出的寄存器对应栈上哪块区域
    private int spilledSize = 0;

    public LinearRegAlloc(ObjModule objModule) {
        this.objModule = objModule;
    }

    //1给函数内的所有指令编号
    //2计算每个变量(vir)的活跃范围
    //3分配s0,t0
    //多的存栈上

    public void run() {
        for (ObjFunction objFunction : objModule.getFunctions()) {
            currentFunc = objFunction;
            getNumber();//编号
            getActive();//计算活跃范围
            allocRegs();//分配
            //分配时，只需在函数开头的分配栈空间处多加一些，然后从原来栈空间结束处开始分配即可
            addSpilled();//补充栈指令
        }
    }

    public void getNumber() {
        //num->instr
        //num->block(重新赋值整个block的语句)
        instrs.clear();
        blocks.clear();
        activeRange.clear();
        for (ObjBlock objBlock : currentFunc.getBlocks()) {
            for (ObjInstr objInstr : objBlock.getInstrs()) {
                instrs.add(objInstr);
                blocks.add(objBlock);
            }
        }
    }

    public void getActive() {
        for (int i = 0; i < instrs.size(); i++) {
            ObjInstr objInstr = instrs.get(i);
            if (objInstr instanceof ObjBinary binary) {
                renewActive(binary.getDst(), i);
                renewActive(binary.getSrc1(), i);
                renewActive(binary.getSrc2(), i);
            } else if (objInstr instanceof ObjBranch branch) {
                renewActive(branch.getReg(), i);
            } else if (objInstr instanceof ObjJ j) {
                renewActive(j.getOperand(), i);
            } else if (objInstr instanceof ObjLoad load) {
                renewActive(load.getDst(), i);
                renewActive(load.getAddr(), i);
                renewActive(load.getOffset(), i);
            } else if (objInstr instanceof ObjMove move) {
                renewActive(move.getDst(), i);
                renewActive(move.getSrc(), i);
            } else if (objInstr instanceof ObjStore store) {
                renewActive(store.getPointer(), i);
                renewActive(store.getValue(), i);
                renewActive(store.getOffset(), i);
            }
        }
    }

    public void renewActive(ObjOperand operand, int i) {
        if (operand instanceof ObjVirReg reg) {
            if (activeRange.containsKey(reg)) {
                Pair<Integer, Integer> range = activeRange.get(reg);
                Pair<Integer, Integer> newRange = new Pair<>(range.getFirst(), i);
                activeRange.replace(reg, newRange);
            } else {
                activeRange.put(reg, new Pair<>(i, i));
            }
        }
    }

    public void allocRegs() {
        allocated.clear();
        //freeRegs.clear();
        spilledSize = 0;
        //freeRegs.addAll(ObjPhyReg.regs.subList(16, 24));
        phyRegRange.clear();
        for (int i = 16; i < 24; i++) {
            phyRegRange.put(ObjPhyReg.regs.get(i), new LinkedList<>());
        }
        //TODO，目前直接分配了所有寄存器，栈的操作只对应t0,之后还要全局、局部按需分配
        for (int i = 0; i < instrs.size(); i++) {
            //renewFreeRegs(i);
            ObjInstr objInstr = instrs.get(i);
            if (objInstr instanceof ObjBinary binary) {
                binary.setDst(allocReg(binary.getDst()));
                binary.setSrc1(allocReg(binary.getSrc1()));
                binary.setSrc2(allocReg(binary.getSrc2()));
            } else if (objInstr instanceof ObjBranch branch) {
                branch.setReg((ObjReg) allocReg(branch.getReg()));
            } else if (objInstr instanceof ObjJ j) {
                j.setOperand(allocReg(j.getOperand()));
            } else if (objInstr instanceof ObjLoad load) {
                load.setDst(allocReg(load.getDst()));
                load.setAddr(allocReg(load.getAddr()));
                load.setOffset(allocReg(load.getOffset()));
            } else if (objInstr instanceof ObjMove move) {
                move.setDst(allocReg(move.getDst()));
                move.setSrc(allocReg(move.getSrc()));
            } else if (objInstr instanceof ObjStore store) {
                store.setPointer(allocReg(store.getPointer()));
                store.setValue(allocReg(store.getValue()));
                store.setOffset(allocReg(store.getOffset()));
            }
        }
    }

    public void renewFreeRegs(int i) {
        Iterator<Map.Entry<ObjVirReg, ObjPhyReg>> it = allocated.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ObjVirReg, ObjPhyReg> entry = it.next();
            ObjVirReg reg = entry.getKey();
            if (i < activeRange.get(reg).getFirst() || i > activeRange.get(reg).getSecond()) {
                //freeRegs.add(entry.getValue());
                it.remove();
            }
        }
    }

    public ObjOperand allocReg(ObjOperand operand) {
        if (!(operand instanceof ObjVirReg reg)) {
            return operand;
        }
        if (allocated.containsKey(reg)) {
            return allocated.get(reg);//分配过物理寄存器
        } else if (spilled.containsKey(reg)) {
            return reg;//分配过栈空间
        }
//        for (int i = 16; i < 24; i++) {
//            ObjPhyReg phyReg = ObjPhyReg.regs.get(i);
//            boolean flag = false;//是否冲突
//            for (Pair<Integer, Integer> pair : phyRegRange.get(phyReg)) {
//                if (!(activeRange.get(reg).getFirst() > pair.getSecond() || activeRange.get(reg).getSecond() < pair.getFirst())) {
//                    flag = true;
//                    break;
//                }
//            }
//            if (!flag) {
//                allocated.put(reg, phyReg);
//                phyRegRange.get(phyReg).add(activeRange.get(reg));
//                return phyReg;
//            }
//        }

        spilled.put(reg, spilledSize);
        spilledSize += 4;
        return operand;
//        if (!freeRegs.isEmpty()) {
//            allocated.put(reg, freeRegs.pop());//有多余物理寄存器
//            return allocated.get(reg);
//        } else {
//            spilled.put(reg, spilledSize);
//            spilledSize += 4;
//            return operand;
//        }
    }

    public void addSpilled() {
        int blockNum = 0;
        LinkedList<ObjInstr> newInstrs = new LinkedList<>();
        ObjPhyReg t0 = ObjPhyReg.nameToReg.get("t0");
        ObjPhyReg t1 = ObjPhyReg.nameToReg.get("t1");
        ObjPhyReg t2 = ObjPhyReg.nameToReg.get("t2");
        ObjPhyReg sp = ObjPhyReg.nameToReg.get("sp");
        for (int i = 0; i < instrs.size(); i++) {
            if (!blocks.get(blockNum).equals(blocks.get(i))) {
                blocks.get(blockNum).resetInstrs(newInstrs);
                blockNum = i;
                newInstrs = new LinkedList<>();
            }
            ObjInstr objInstr = instrs.get(i);
            if (objInstr instanceof ObjBinary binary) {
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
            } else if (objInstr instanceof ObjBranch branch) {
                if (branch.getReg() instanceof ObjVirReg vreg) {
                    newInstrs.add(new ObjLoad("lw", t0, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg))));
                    branch.setReg(t0);
                }
                newInstrs.add(branch);
            } else if (objInstr instanceof ObjJ j) {
                if (j.getOperand() instanceof ObjVirReg vreg) {
                    newInstrs.add(new ObjLoad("lw", t0, sp, new ObjImm(currentFunc.getStackSize() + spilled.get(vreg))));
                    j.setOperand(t0);
                }
                newInstrs.add(j);
            } else if (objInstr instanceof ObjLoad load) {
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
            } else if (objInstr instanceof ObjMove move) {
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
            } else if (objInstr instanceof ObjStore store) {
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
                newInstrs.add(objInstr);
            }
        }
        blocks.get(blockNum).resetInstrs(newInstrs);
    }
}
