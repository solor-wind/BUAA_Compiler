package midOPT;

import ir.instr.*;
import ir.value.*;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DCE {
    private IRModule irModule;
    private CFG cfg;
    private Function currentFunction;
    private HashMap<Value, Pair<BasicBlock, Integer>> defMap;//每个变量的定义点
    private HashSet<Value> usedValues;

    public DCE(IRModule irModule, CFG cfg) {
        this.irModule = irModule;
        this.cfg = cfg;
    }

    public void run() {
        deleteFunctions();
        for (Function f : irModule.functions) {
            currentFunction = f;
            defMap = new HashMap<>();
            usedValues = new HashSet<>();
            getUseDef();
            getMaxUses();
            deleteInstrs();
        }
    }

    public void deleteFunctions() {
        HashSet<Function> visited = new HashSet<>();
        funcDFS(irModule.getFunction("main"), visited);
        ArrayList<Function> functions = new ArrayList<>();
        for (Function f : irModule.functions) {
            if (visited.contains(f)) {
                functions.add(f);
            }
        }
        irModule.functions = functions;
    }

    public void funcDFS(Function function, HashSet<Function> visited) {
        if (visited.contains(function)) {
            return;
        }
        visited.add(function);
        for (Function f : function.getCalledFunctions()) {
            funcDFS(f, visited);
        }
    }

    //获取use-def关系
    //哪个块的哪条指令使用了哪个块的哪条指令的操作数
    public void getUseDef() {
        for (BasicBlock block : currentFunction.getBlocks()) {
            ArrayList<Instruction> instrs = block.getInstructions();
            for (int i = 0; i < instrs.size(); i++) {
                for (Value v : instrs.get(i).defs) {
                    defMap.put(v, new Pair<>(block, i));
                }
                if (instrs.get(i) instanceof CallInstr call) {
                    usedValues.addAll(call.uses);
//                    if (IRBuilder.libFunctions.containsKey(call.getFunction().getName().substring(1))) {
//                        usedValues.addAll(instrs.get(i).uses);//输入输出
//                    }
                } else if (instrs.get(i) instanceof RetInstr ret) {
                    usedValues.addAll(ret.uses);
                } else if (instrs.get(i) instanceof BrInstr br) {
                    usedValues.addAll(br.uses);
                } else if (instrs.get(i) instanceof StoreInstr || instrs.get(i) instanceof LoadInstr || instrs.get(i) instanceof GetPtrInstr) {
                    usedValues.addAll(instrs.get(i).uses);
                }
            }
        }
    }

    public void getMaxUses() {
        HashSet<Value> tmp = (HashSet<Value>) usedValues.clone();
        for (Value v : tmp) {
            if (defMap.containsKey(v)) {
                valueDFS(v);
            }
        }
    }

    public void valueDFS(Value v) {
        Pair<BasicBlock, Integer> pair = defMap.get(v);
        if (pair == null) {
            return;
        }
        HashSet<Value> tmpUses = pair.getFirst().getInstructions().get(pair.getSecond()).uses;
        for (Value value : tmpUses) {
            if (!usedValues.contains(value)) {
                usedValues.add(value);
                valueDFS(value);
            }
        }
    }

    public void deleteInstrs() {
        for (BasicBlock block : currentFunction.getBlocks()) {
            ArrayList<Instruction> instrs = new ArrayList<>();
            for (Instruction instr : block.getInstructions()) {
                boolean flag = false;
                for (Value v : instr.defs) {
                    if (usedValues.contains(v)) {
                        flag = true;
                        break;
                    }
                }
                if (flag || instr instanceof BrInstr || instr instanceof CallInstr || instr instanceof RetInstr) {
                    //TODO:可优化
                    instrs.add(instr);
                } else if (instr instanceof StoreInstr || instr instanceof LoadInstr || instr instanceof GetPtrInstr) {
                    instrs.add(instr);
                }
            }
            block.setInstructions(instrs);
        }
    }
}
//对全局变量做出的改动不能删
/*
函数可以被删掉的条件：
    1.没有输入输出
    2.没有改变全局变量，或者改变的全局变量没有使用
    3.调用的子函数也满足1、2条
    4.返回值没有被使用
1.获取全局变量使用情况——得到每个函数的使用、修改情况
2.
*/