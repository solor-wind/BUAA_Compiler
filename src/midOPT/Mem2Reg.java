package midOPT;

import ir.instr.*;
import ir.type.IntegerType;
import ir.type.PointerType;
import ir.value.*;
import utils.Pair;

import java.util.*;

public class Mem2Reg {
    private IRModule irModule;
    private DomTree domTree;
    private CFG cfg;
    private HashMap<Value, HashSet<BasicBlock>> defs = new HashMap<>();//每个局部变量在哪里被定义，存的是指针形式
    private HashMap<Value, HashSet<Value>> localMap = new HashMap<>();//局部变量-phi的变量的映射
    private HashMap<Value, Value> phiValMap = new HashMap<>();//phi的变量-局部变量的映射
    private HashMap<Value, Value> localValMap = new HashMap<>();//局部变量的最新值
    private HashMap<Value, Value> loadValMap = new HashMap<>();//load指令load出的局部变量的临时变量对应的值
    private Function currentFunction;

    public Mem2Reg(IRModule irModule, DomTree domTree, CFG cfg) {
        this.irModule = irModule;
        this.domTree = domTree;
        this.cfg = cfg;
    }

    public Mem2Reg(IRModule irModule, DomTree domTree) {
        this.irModule = irModule;
        this.domTree = domTree;
    }

    public void run() {
        for (Function function : irModule.functions) {
            currentFunction = function;
            init();
            getDefs();//获取每个局部变量的定义块
            insertPhi();//插入phi指令
            rename(currentFunction.getBlocks().get(0));//变量重命名，删除alloca
            addPhiLabel();//默认所有变量都在开头定义过，补充一下仅有一个label的phi指令
        }
    }

    private void init() {
        defs.clear();
        localMap.clear();
        phiValMap.clear();
        localValMap.clear();
        loadValMap.clear();
    }

    public void getDefs() {
        for (BasicBlock block : currentFunction.getBlocks()) {
            for (Instruction instr : block.getInstructions()) {
                if (instr instanceof AllocaInstr alloca) {
                    Variable v = alloca.getVar();
                    if (!v.isArray()) {
                        if (!defs.containsKey(v)) {
                            defs.put(v, new HashSet<>());
                        }
                        defs.get(v).add(block);
                    }
                } else if (instr instanceof StoreInstr store) {
                    Value v = store.getAddr();
                    if (defs.containsKey(v)) {
                        defs.get(v).add(block);//TODO遍历一次保险吗？能确保alloca都在def前面吗
                    }
                }
            }
        }

    }

    public void insertPhi() {
        for (Value v : defs.keySet()) {
            int index = 0;
            localMap.put(v, new HashSet<>());
            HashSet<BasicBlock> F = new HashSet<>();
            HashSet<BasicBlock> W = (HashSet<BasicBlock>) defs.get(v).clone();
            while (!W.isEmpty()) {
                BasicBlock X = W.iterator().next();
                W.remove(X);
                for (BasicBlock Y : domTree.DF.get(X)) {
                    if (!F.contains(Y)) {
                        Variable val = new Variable(v.getName() + "_" + index++, ((PointerType) v.getType()).getBaseType());
                        Y.addFirstInstruction(new PhiInstr(val));
                        localMap.get(v).add(val);
                        phiValMap.put(val, v);
                        F.add(Y);
                        if (!defs.get(v).contains(Y)) {
                            W.add(Y);
                        }
                    }
                }
            }
        }
    }

    public void rename(BasicBlock block) {
        ArrayList<Instruction> newInstrs = new ArrayList<>();
        for (Instruction instr : block.getInstructions()) {
            replaceValue(instr);
            if (instr instanceof AllocaInstr alloca) {
                if (defs.containsKey(alloca.getVar())) {
                    localValMap.put(alloca.getVar(), new Literal(0, ((PointerType) alloca.getVar().getType()).getBaseType()));
                    //局部变量未定义就使用，文法没说，但默认为0
                    continue;
                }
            } else if (instr instanceof StoreInstr store) {
                if (defs.containsKey(store.getAddr())) {
                    localValMap.put(store.getAddr(), store.getValue());
                    continue;
                }
            } else if (instr instanceof LoadInstr load) {
                if (defs.containsKey(load.getAddr())) {
                    loadValMap.put(load.getRes(), localValMap.get(load.getAddr()));
                    continue;
                }
            } else if (instr instanceof PhiInstr phi) {
                localValMap.put(phiValMap.get(phi.getRes()), phi.getRes());//更新局部变量对应的值
            }
            newInstrs.add(instr);
        }
        block.setInstructions(newInstrs);
        for (BasicBlock nextBlock : cfg.nexts.get(block)) {
            for (Instruction instr : nextBlock.getInstructions()) {
                if (instr instanceof PhiInstr phi) {
                    if (localValMap.containsKey(phiValMap.get(phi.getRes()))) {
                        phi.addValLabel(new Pair<>(localValMap.get(phiValMap.get(phi.getRes())), block));
                    }
                } else {
                    break;
                }
            }
        }
        for (BasicBlock domBlock : domTree.domTree.get(block)) {
            HashMap<Value, Value> tmpMap = (HashMap<Value, Value>) localValMap.clone();
            rename(domBlock);//前序遍历支配树，每个子节点使用相同的当前父节点的stackMap（子节点1更新时不能影响子节点2）
            localValMap = tmpMap;
        }
    }

    /*public void dfs(BasicBlock block) {
        LinkedList<Instruction> newInstrs = new LinkedList<>();
        for (Instruction instr : block.getInstructions()) {
            replaceValue(instr, loadVal);
            if (instr instanceof AllocaInstr alloca) {
                if (defs.containsKey(alloca.getVar())) {
                    continue;
                }
            } else if (instr instanceof StoreInstr store) {
                if (defs.containsKey(store.getAddr())) {
                    stackMap.put(store.getAddr(), store.getValue());
                    continue;
                }
            } else if (instr instanceof LoadInstr load) {
                if (defs.containsKey(load.getAddr())) {
                    loadVal.put(load.getAddr(), storeVal.get(load.getAddr()));
                    continue;
                }
            }
            newInstrs.add(instr);
        }
    }*/

    public void replaceValue(Instruction instr) {
        //TODO:没有替换的：getPtr,load,store的addr，以及产生的值
        if (instr instanceof AllocaInstr alloca) {
            ;
        } else if (instr instanceof BinaInstr bina) {
//            if (loadVal.containsKey(bina.getRes())) {
//                bina.setRes(loadVal.get(bina.getRes()));
//            }
            if (loadValMap.containsKey(bina.getVal1())) {
                bina.setVal1(loadValMap.get(bina.getVal1()));
            }
            if (loadValMap.containsKey(bina.getVal2())) {
                bina.setVal2(loadValMap.get(bina.getVal2()));
            }
        } else if (instr instanceof BrInstr br) {
            if (loadValMap.containsKey(br.getCond())) {
                br.setCond(loadValMap.get(br.getCond()));
            }
        } else if (instr instanceof CallInstr call) {
            ArrayList<Argument> args = call.getArguments();
            for (int i = 0; i < args.size(); i++) {
                if (loadValMap.containsKey(args.get(i).value)) {
                    call.changeUses(i, loadValMap.get(args.get(i).value));
                    args.set(i, new Argument(loadValMap.get(args.get(i).value)));
                }
            }
        } else if (instr instanceof GetPtrInstr getPtr) {
            if (loadValMap.containsKey(getPtr.getOffset())) {
                getPtr.setOffset(loadValMap.get(getPtr.getOffset()));
            }
        } else if (instr instanceof IcmpInstr icmp) {
            if (loadValMap.containsKey(icmp.getLv())) {
                icmp.setLv(loadValMap.get(icmp.getLv()));
            }
            if (loadValMap.containsKey(icmp.getRv())) {
                icmp.setRv(loadValMap.get(icmp.getRv()));
            }
        } else if (instr instanceof LabelInstr label) {
            ;
        } else if (instr instanceof LoadInstr load) {
            ;
        } else if (instr instanceof RetInstr ret) {
            if (loadValMap.containsKey(ret.getValue())) {
                ret.setValue(loadValMap.get(ret.getValue()));
            }
        } else if (instr instanceof StoreInstr store) {
            if (loadValMap.containsKey(store.getValue())) {
                store.setValue(loadValMap.get(store.getValue()));
            }
        } else if (instr instanceof TruncInstr trunc) {
            if (loadValMap.containsKey(trunc.getVal())) {
                trunc.setVal(loadValMap.get(trunc.getVal()));
            }
        } else if (instr instanceof ZextInstr zext) {
            if (loadValMap.containsKey(zext.getVal())) {
                zext.setVal(loadValMap.get(zext.getVal()));
            }
        }
    }

    public void addPhiLabel() {
        for (BasicBlock block : currentFunction.getBlocks()) {
            for (Instruction instr : block.getInstructions()) {
                if (instr instanceof PhiInstr phi) {
                    if (phi.getValLabel().size() < 2) {
                        HashSet<BasicBlock> tmp = new HashSet<>();
                        for (Pair<Value, BasicBlock> pair : phi.getValLabel()) {
                            tmp.add(pair.getSecond());
                        }
                        Literal l = new Literal(0, phi.getRes().getType());
                        for (BasicBlock prev : cfg.prevs.get(block)) {
                            if (tmp.contains(prev)) {
                                continue;
                            }
                            phi.addValLabel(new Pair<>(l, prev));
                        }
                    }
                } else {
                    break;
                }
            }
        }
    }
}
