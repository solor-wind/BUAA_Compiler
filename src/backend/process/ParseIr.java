package backend.process;

import backend.Backend;
import backend.component.*;
import backend.instruction.*;
import backend.operand.*;
import ir.instr.*;
import ir.type.IntegerType;
import ir.type.PointerType;
import ir.value.*;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class ParseIr {
    private IRModule irModule;
    private ObjModule objModule;
    private HashMap<String, ObjGlobalVar> globalMap = new HashMap<>();
    private HashMap<String, ObjFunction> functionMap = new HashMap<>();

    private ObjFunction currentFunction;
    private ObjBlock currentBlock;

    public ParseIr(IRModule irModule, ObjModule objModule) {
        this.irModule = irModule;
        this.objModule = objModule;
    }

    public void parse() {
        parseGlobalVars();
        parseFunctions();
    }

    public void parseGlobalVars() {
        for (Variable var : irModule.globalVariables) {
            ObjGlobalVar objGlobalVar = new ObjGlobalVar(var.getName().replace("@", "g"), var.getType());
            globalMap.put(var.getName(), objGlobalVar);
            if (var.getInitValue() instanceof Integer i) {
                objGlobalVar.addInit(i);
            } else if (var.getStringConst() != null) {
                objGlobalVar.setStringConst((String) var.getInitValue());
            } else {
                objGlobalVar.setInits((ArrayList<Integer>) var.getInitValue());
            }
            objModule.addGlobalVar(objGlobalVar);
        }
    }

    public void parseFunctions() {
        for (Function function : irModule.functions) {
            ObjFunction objFunction;
            if (functionMap.containsKey(function.getName().substring(1))) {
                objFunction = functionMap.get(function.getName());
            } else {
                objFunction = new ObjFunction(function.getName().substring(1));
                functionMap.put(objFunction.getName(), objFunction);
            }
            currentFunction = objFunction;
            objModule.addFunction(objFunction);
            HashMap<Value, ObjReg> opMap = new HashMap<>();
            HashMap<Value, ObjBlock> blockMap = new HashMap<>();
            ObjBlock.blockIndex = 0;
            solveStack(objFunction, function, opMap);

            for (BasicBlock block : function.getBlocks()) {
                ObjBlock objBlock;
                if (blockMap.containsKey(block)) {
                    objBlock = blockMap.get(block);
                } else {
                    objBlock = new ObjBlock(objFunction);
                    blockMap.put(block, objBlock);
                }
                currentBlock = objBlock;
                objFunction.addBlock(objBlock);

                for (Instruction instr : block.getInstructions()) {
                    if (instr instanceof AllocaInstr allocaInstr) {
                        parseAlloca(objBlock, opMap, allocaInstr, objFunction);
                    } else if (instr instanceof BinaInstr binaInstr) {
                        parseBina(objBlock, opMap, binaInstr);
                    } else if (instr instanceof BrInstr brInstr) {
                        parseBr(objBlock, opMap, brInstr, blockMap);
                    } else if (instr instanceof CallInstr callInstr) {
                        parseCall(opMap, callInstr);
                    } else if (instr instanceof GetPtrInstr getPtrInstr) {
                        parseGetPtr(opMap, getPtrInstr);
                    } else if (instr instanceof IcmpInstr icmpInstr) {
                        parseIcmp(opMap, icmpInstr);
                    } else if (instr instanceof LoadInstr loadInstr) {
                        parseLoad(opMap, loadInstr);
                    } else if (instr instanceof PCInstr pc) {
                        parsePC(opMap, pc);
                    } else if (instr instanceof RetInstr retInstr) {
                        parseRet(opMap, retInstr);
                    } else if (instr instanceof StoreInstr storeInstr) {
                        parseStore(opMap, storeInstr);
                    } else if (instr instanceof TruncInstr || instr instanceof ZextInstr) {
                        parseChangeType(opMap, instr);
                    }
                }
            }
        }
    }

    public void solveStack(ObjFunction objFunction, Function function, HashMap<Value, ObjReg> opMap) {
        for (BasicBlock block : function.getBlocks()) {
            for (Instruction instr : block.getInstructions()) {
                if (instr instanceof AllocaInstr allocaInstr) {
                    int size = allocaInstr.getSize();
                    if (allocaInstr.getVar().getType().equals(new PointerType(new IntegerType(32)))) {
                        size *= 4;
                    }
                    int padding = 4 - size % 4;
                    objFunction.allocaSize += size + padding;
                } else if (instr instanceof CallInstr callInstr) {
                    int argSize = callInstr.getArguments().size() * 4;
                    if (objFunction.argSize < argSize) {
                        objFunction.argSize = argSize;
                    }
                }
            }
        }
        ObjBlock objBlock = new ObjBlock(objFunction);
        objBlock.addInstr(new ObjBinary("addi", ObjPhyReg.SP, ObjPhyReg.SP, new ObjImm(-currentFunction.getStackSize())));
        if (!objFunction.getName().equals("main")) {
            objBlock.addInstr(new ObjStore("sw", ObjPhyReg.RA, ObjPhyReg.SP, new ObjImm(currentFunction.argSize + currentFunction.regSize)));
        }
        /*加载参数*/
        for (int i = 0; i < function.getArguments().size(); i++) {
            Argument arg = function.getArguments().get(i);
            if (i < 4) {
                objBlock.addInstr(new ObjMove("move", parseOperand(arg, opMap), ObjPhyReg.nameToReg.get("a" + i)));
            } else {
                if (arg.getType().equals(new IntegerType(8))) {
                    objBlock.addInstr(new ObjLoad("lb", parseOperand(arg, opMap), ObjPhyReg.SP, new ObjImm(currentFunction.getStackSize() + 4 * (i - 4))));
                } else {
                    objBlock.addInstr(new ObjLoad("lw", parseOperand(arg, opMap), ObjPhyReg.SP, new ObjImm(currentFunction.getStackSize() + 4 * (i - 4))));
                }
            }
        }
        objFunction.addBlock(objBlock);
    }

    public ObjOperand parseOperand(Value value, HashMap<Value, ObjReg> opMap) {
        if (value instanceof Argument argument) {
            if (argument.value != null) {
                value = argument.value;
            }
        }
        if (globalMap.containsKey(value.getName())) {
            return new ObjLabel(globalMap.get(value.getName()));
        }
        if (value instanceof Literal l) {
            return new ObjImm(l.getValue());
        }
        if (!opMap.containsKey(value)) {
            opMap.put(value, new ObjVirReg());
        }
        return opMap.get(value);
    }

    public void parseAlloca(ObjBlock objBlock, HashMap<Value, ObjReg> opMap, AllocaInstr allocaInstr, ObjFunction objFunction) {
        //%v0=alloca i32
        //la vr0,sp+4
        ObjOperand dst = parseOperand(allocaInstr.getVar(), opMap);
        int size = allocaInstr.getSize();
        if (allocaInstr.getVar().getType().equals(new PointerType(new IntegerType(32)))) {
            size *= 4;
        }
        int padding = 4 - size % 4;
        int offset = objFunction.getNextAlloca(size + padding);
        objBlock.addInstr(new ObjBinary("addi", dst, ObjPhyReg.SP, new ObjImm(offset)));
        //objBlock.addInstr(new ObjLoad("la", dst, ObjPhyReg.SP, new ObjImm(offset)));
    }

    public void parseBina(ObjBlock objBlock, HashMap<Value, ObjReg> opMap, BinaInstr binaInstr) {
        ObjOperand res = parseOperand(binaInstr.getRes(), opMap);
        ObjOperand val1 = parseOperand(binaInstr.getVal1(), opMap);
        ObjOperand val2 = parseOperand(binaInstr.getVal2(), opMap);
        if (val1 instanceof ObjImm imm1 && val2 instanceof ObjImm imm2) {
            currentBlock.addInstr(new ObjMove("li", res, new ObjImm(
                    switch (binaInstr.getName()) {
                        case "add" -> imm1.getImmediate() + imm2.getImmediate();
                        case "sub" -> imm1.getImmediate() - imm2.getImmediate();
                        case "mul" -> imm1.getImmediate() * imm2.getImmediate();
                        case "sdiv" -> imm1.getImmediate() / imm2.getImmediate();
                        case "srem" -> imm1.getImmediate() % imm2.getImmediate();
                        default -> 0;
                    }
            )));
            return;
        }
        switch (binaInstr.getName()) {
            case "add":
                if (val1 instanceof ObjImm) {
                    ObjOperand tmp = val1;
                    val1 = val2;
                    val2 = tmp;
                }
                if (val2 instanceof ObjImm) {
                    objBlock.addInstr(new ObjBinary("addi", res, val1, val2));
                } else {
                    objBlock.addInstr(new ObjBinary("add", res, val1, val2));
                }
                break;
            case "sub":
                if (val1 instanceof ObjImm) {
                    ObjVirReg virReg = new ObjVirReg();
                    objBlock.addInstr(new ObjMove("li", virReg, val1));
                    objBlock.addInstr(new ObjBinary("sub", res, virReg, val2));
                } else if (val2 instanceof ObjImm objImm) {
                    objBlock.addInstr(new ObjBinary("addi", res, val1, new ObjImm(-objImm.getImmediate())));
                } else {
                    objBlock.addInstr(new ObjBinary("sub", res, val1, val2));
                }
                break;
            case "mul":
                if (val1 instanceof ObjImm) {
                    ObjOperand tmp = val1;
                    val1 = val2;
                    val2 = tmp;
                }
                if (val2 instanceof ObjImm imm) {
                    int a = imm.getImmediate();
                    if (a == 0) {
                        objBlock.addInstr(new ObjMove("move", res, ObjPhyReg.ZERO));
                        break;
                    } else if (a == 1) {
                        objBlock.addInstr(new ObjMove("move", res, val1));
                        break;
                    } else if (a == -1) {
                        objBlock.addInstr(new ObjBinary("sub", res, ObjPhyReg.ZERO, val1));
                        break;
                    } else if (a > 0 && ((a & (a - 1)) == 0)) {
                        int tmp = -1;
                        while (a > 0) {
                            tmp++;
                            a /= 2;
                        }
                        objBlock.addInstr(new ObjBinary("sll", res, val1, new ObjImm(tmp)));
                        break;
                    }
                }
                objBlock.addInstr(new ObjBinary("mul", res, val1, val2));
                break;
            case "sdiv":
                if (val1 instanceof ObjImm imm) {
                    if (imm.getImmediate() == 0) {
                        objBlock.addInstr(new ObjMove("move", res, ObjPhyReg.ZERO));
                        break;
                    }
                    ObjVirReg virReg = new ObjVirReg();
                    objBlock.addInstr(new ObjMove("li", virReg, val1));
                    objBlock.addInstr(new ObjBinary("div", res, virReg, val2));
                } else {
                    if (val2 instanceof ObjImm imm) {
                        long a = imm.getImmediate();
                        if (a == 1) {
                            objBlock.addInstr(new ObjMove("move", res, val1));
                            break;
                        } else if (a == -1) {
                            objBlock.addInstr(new ObjBinary("sub", res, ObjPhyReg.ZERO, val1));
                            break;
                        }
                    }
                    objBlock.addInstr(new ObjBinary("div", res, val1, val2));
                }
                break;
            case "srem":
                if (val1 instanceof ObjImm) {
                    ObjVirReg virReg = new ObjVirReg();
                    objBlock.addInstr(new ObjMove("li", virReg, val1));
                    objBlock.addInstr(new ObjBinary("sub", res, virReg, val2));
                } else {
                    if (val2 instanceof ObjImm imm) {
                        long a = imm.getImmediate();
                        if (a == 1 || a == -1) {
                            objBlock.addInstr(new ObjMove("move", res, ObjPhyReg.ZERO));
                            break;
                        }
                    }
                    objBlock.addInstr(new ObjBinary("rem", res, val1, val2));
                }
                break;
        }
    }

    public void parseBr(ObjBlock objBlock, HashMap<Value, ObjReg> opMap, BrInstr brInstr, HashMap<Value, ObjBlock> blockMap) {
        //br %1,b1,b2
        //beqz b2
        //j b1
        if (brInstr.getCond() != null) {
            ObjReg reg = (ObjReg) parseOperand(brInstr.getCond(), opMap);
            ObjBlock b2;
            if (blockMap.containsKey(brInstr.getBlock2())) {
                b2 = blockMap.get(brInstr.getBlock2());
            } else {
                b2 = new ObjBlock(currentFunction);
                blockMap.put(brInstr.getBlock2(), b2);
            }
            objBlock.addInstr(new ObjBranch("beqz", reg, new ObjLabel(b2)));
            objBlock.addNextBlock(b2);
            b2.addPreBlock(objBlock);
        }

        if (blockMap.containsKey(brInstr.getBlock1())) {
            ObjBlock b1 = blockMap.get(brInstr.getBlock1());
            objBlock.addInstr(new ObjJ("j", new ObjLabel(b1)));
            objBlock.addNextBlock(b1);
            b1.addPreBlock(objBlock);
        } else {
            ObjBlock newBlock = new ObjBlock(currentFunction);
            blockMap.put(brInstr.getBlock1(), newBlock);
            objBlock.addInstr(new ObjJ("j", new ObjLabel(newBlock)));
            objBlock.addNextBlock(newBlock);
            newBlock.addPreBlock(objBlock);
        }
    }

    public void parseCall(HashMap<Value, ObjReg> opMap, CallInstr callInstr) {
        switch (callInstr.getFunction().getName().substring(1)) {
            case "getint":
            case "getchar":
            case "putint":
            case "putch":
            case "putstr":
                parseLibFunc(opMap, callInstr);
                return;
        }
        /*
         * 先存reg
         * 再传参数
         * 最后调用
         * */

        if (!Backend.graphColor) {
            currentBlock.addInstr(new ObjStore("sw", ObjPhyReg.RA, ObjPhyReg.SP, new ObjImm(currentFunction.argSize + currentFunction.regSize)));
            for (int i = 0; i < 32; i += 4) {
                currentBlock.addInstr(new ObjStore("sw", ObjPhyReg.regs.get(16 + i / 4), ObjPhyReg.SP, new ObjImm(currentFunction.argSize + i)));
            }
        }
        int argNum = 0;
        ArrayList<ObjInstr> tmpInstrs = new ArrayList<>();
        for (Argument arg : callInstr.getArguments()) {
            ObjOperand operand = parseOperand(arg, opMap);
            if (argNum < 4) {
                if (operand instanceof ObjImm) {
                    tmpInstrs.add(new ObjMove("li", ObjPhyReg.regs.get(4 + argNum), operand));
                    //currentBlock.addInstr(new ObjMove("li", ObjPhyReg.regs.get(4 + argNum), operand));
                } else {
                    tmpInstrs.add(new ObjMove("move", ObjPhyReg.regs.get(4 + argNum), operand));
                    //currentBlock.addInstr(new ObjMove("move", ObjPhyReg.regs.get(4 + argNum), operand));
                }
            } else {
                if (operand instanceof ObjImm) {
                    ObjVirReg reg = new ObjVirReg();
                    currentBlock.addInstr(new ObjMove("li", reg, operand));
                    operand = reg;
                }
                currentBlock.addInstr(new ObjStore("sw", operand, ObjPhyReg.SP, new ObjImm((argNum - 4) * 4)));
            }
            argNum++;
        }
        //最后传前四个
        for (ObjInstr tmpInstr : tmpInstrs) {
            currentBlock.addInstr(tmpInstr);
        }

        if (functionMap.containsKey(callInstr.getFunction().getName().substring(1))) {
            currentBlock.addInstr(new ObjJ("jal", new ObjLabel(functionMap.get(callInstr.getFunction().getName().substring(1)))));
        } else {
            ObjFunction objFunction = new ObjFunction(callInstr.getFunction().getName().substring(1));
            functionMap.put(objFunction.getName(), objFunction);
            currentBlock.addInstr(new ObjJ("jal", new ObjLabel(objFunction)));
        }


        if (!Backend.graphColor) {
            currentBlock.addInstr(new ObjLoad("lw", ObjPhyReg.RA, ObjPhyReg.SP, new ObjImm(currentFunction.argSize + currentFunction.regSize)));
            for (int i = 0; i < 32; i += 4) {
                currentBlock.addInstr(new ObjLoad("lw", ObjPhyReg.regs.get(16 + i / 4), ObjPhyReg.SP, new ObjImm(currentFunction.argSize + i)));
            }
        }
        if (callInstr.getRes() != null) {
            ObjOperand res = parseOperand(callInstr.getRes(), opMap);
            currentBlock.addInstr(new ObjMove("move", res, ObjPhyReg.nameToReg.get("v0")));
        }
    }

    public void parseLibFunc(HashMap<Value, ObjReg> opMap, CallInstr callInstr) {
        ObjOperand res = null, val = null;
        if (callInstr.getRes() != null) {
            res = parseOperand(callInstr.getRes(), opMap);
        }
        if (!callInstr.getArguments().isEmpty()) {
            val = parseOperand(callInstr.getArguments().get(0), opMap);
        }
        ObjPhyReg v0 = ObjPhyReg.nameToReg.get("v0");
        ObjPhyReg a0 = ObjPhyReg.nameToReg.get("a0");
        switch (callInstr.getFunction().getName().substring(1)) {
            case "getint":
                currentBlock.addInstr(new ObjMove("li", v0, new ObjImm(5)));
                currentBlock.addInstr(new ObjSyscall());
                currentBlock.addInstr(new ObjMove("move", res, v0));
                break;
            case "getchar":
                currentBlock.addInstr(new ObjMove("li", v0, new ObjImm(12)));
                currentBlock.addInstr(new ObjSyscall());
                currentBlock.addInstr(new ObjMove("move", res, v0));
                break;
            case "putint":
                currentBlock.addInstr(new ObjMove("li", v0, new ObjImm(1)));
                if (val instanceof ObjImm) {
                    currentBlock.addInstr(new ObjMove("li", a0, val));
                } else {
                    currentBlock.addInstr(new ObjMove("move", a0, val));
                }
                currentBlock.addInstr(new ObjSyscall());
                break;
            case "putch":
                currentBlock.addInstr(new ObjMove("li", v0, new ObjImm(11)));
                if (val instanceof ObjImm) {
                    currentBlock.addInstr(new ObjMove("li", a0, val));
                } else {
                    currentBlock.addInstr(new ObjMove("move", a0, val));
                }
                currentBlock.addInstr(new ObjSyscall());
                break;
            case "putstr":
                currentBlock.addInstr(new ObjMove("li", v0, new ObjImm(4)));
                if (val instanceof ObjImm) {
                    currentBlock.addInstr(new ObjMove("li", a0, val));
                } else {
                    currentBlock.addInstr(new ObjMove("move", a0, val));
                }
                currentBlock.addInstr(new ObjSyscall());
                break;
        }
    }

    public void parseGetPtr(HashMap<Value, ObjReg> opMap, GetPtrInstr getPtrInstr) {
        //TODO以后和store,load一起优化一下
        //todo可能有错
        ObjOperand dst = parseOperand(getPtrInstr.getRes(), opMap);
        ObjOperand addr = parseOperand(getPtrInstr.getAddr(), opMap);
        ObjOperand offset = parseOperand(getPtrInstr.getOffset(), opMap);
        if (addr instanceof ObjLabel) {
            if (offset instanceof ObjImm imm) {
                if (getPtrInstr.getRes().getType().equals(new PointerType(new IntegerType(32)))) {
                    imm = new ObjImm(imm.getImmediate() * 4);
                }
                currentBlock.addInstr(new ObjLoad("la", dst, addr, imm));
            } else {
                if (getPtrInstr.getRes().getType().equals(new PointerType(new IntegerType(32)))) {
                    ObjVirReg virReg = new ObjVirReg();
                    currentBlock.addInstr(new ObjBinary("sll", virReg, offset, new ObjImm(2)));
                    offset = virReg;
                }
                currentBlock.addInstr(new ObjLoad("la", dst, addr, offset));
            }
        } else {
            if (offset instanceof ObjImm imm) {
                if (getPtrInstr.getRes().getType().equals(new PointerType(new IntegerType(32)))) {
                    imm = new ObjImm(imm.getImmediate() * 4);
                }
                currentBlock.addInstr(new ObjBinary("addi", dst, addr, imm));
            } else {
                if (getPtrInstr.getRes().getType().equals(new PointerType(new IntegerType(32)))) {
                    ObjVirReg virReg = new ObjVirReg();
                    currentBlock.addInstr(new ObjBinary("sll", virReg, offset, new ObjImm(2)));
                    offset = virReg;
                }
                currentBlock.addInstr(new ObjBinary("add", dst, addr, offset));
            }
        }
    }

    public void parseIcmp(HashMap<Value, ObjReg> opMap, IcmpInstr icmpInstr) {
        //TODO以后和后续的beq一起优化一下
        ObjOperand res = parseOperand(icmpInstr.getRes(), opMap);
        ObjOperand lv = parseOperand(icmpInstr.getLv(), opMap);
        ObjOperand rv = parseOperand(icmpInstr.getRv(), opMap);
        if (lv instanceof ObjImm imm1 && rv instanceof ObjImm imm2) {
            currentBlock.addInstr(new ObjMove("li", res, new ObjImm(
                    switch (icmpInstr.getOp()) {
                        case "eq" -> imm1.getImmediate() == imm2.getImmediate() ? 1 : 0;
                        case "ne" -> imm1.getImmediate() != imm2.getImmediate() ? 1 : 0;
                        case "sgt" -> imm1.getImmediate() > imm2.getImmediate() ? 1 : 0;
                        case "sge" -> imm1.getImmediate() >= imm2.getImmediate() ? 1 : 0;
                        case "slt" -> imm1.getImmediate() < imm2.getImmediate() ? 1 : 0;
                        case "sle" -> imm1.getImmediate() <= imm2.getImmediate() ? 1 : 0;
                        default -> 0;
                    }
            )));
            return;
        }
        //eq,ne,sgt,sge,slt,sle
        if (lv instanceof ObjImm) {
            switch (icmpInstr.getOp()) {
                case "eq" -> currentBlock.addInstr(new ObjBinary("seq", res, rv, lv));
                case "ne" -> currentBlock.addInstr(new ObjBinary("sne", res, rv, lv));
                case "sgt" -> currentBlock.addInstr(new ObjBinary("slti", res, rv, lv));
                case "sge" -> currentBlock.addInstr(new ObjBinary("sle", res, rv, lv));
                case "slt" -> currentBlock.addInstr(new ObjBinary("sgt", res, rv, lv));
                case "sle" -> currentBlock.addInstr(new ObjBinary("sge", res, rv, lv));
            }
            return;
        }
        String immflag = "";
        if (rv instanceof ObjImm) {
            immflag = "i";
        }
        switch (icmpInstr.getOp()) {
            case "eq" -> currentBlock.addInstr(new ObjBinary("seq", res, lv, rv));
            case "ne" -> currentBlock.addInstr(new ObjBinary("sne", res, lv, rv));
            case "sgt" -> currentBlock.addInstr(new ObjBinary("sgt", res, lv, rv));
            case "sge" -> currentBlock.addInstr(new ObjBinary("sge", res, lv, rv));
            case "slt" -> currentBlock.addInstr(new ObjBinary("slt" + immflag, res, lv, rv));
            case "sle" -> currentBlock.addInstr(new ObjBinary("sle", res, lv, rv));
        }
    }

    public void parseLoad(HashMap<Value, ObjReg> opMap, LoadInstr loadInstr) {
        ObjOperand res = parseOperand(loadInstr.getRes(), opMap);
        ObjOperand addr = parseOperand(loadInstr.getAddr(), opMap);
        if (loadInstr.getRes().getType().equals(new IntegerType(8))) {
            currentBlock.addInstr(new ObjLoad("lb", res, addr, new ObjImm(0)));
        } else {
            currentBlock.addInstr(new ObjLoad("lw", res, addr, new ObjImm(0)));
        }
    }

    public void parsePC(HashMap<Value, ObjReg> opMap, PCInstr pc) {
        ArrayList<Pair<Value, Value>> moves = pc.getMoves();
        HashMap<Value, ObjReg> tmp = new HashMap<>();//value对应的临时变量
        for (int i = 0; i < moves.size(); i++) {
            Pair<Value, Value> pair = moves.get(i);
            boolean flag = false;
            for (int j = i + 1; j < moves.size(); j++) {
                Pair<Value, Value> pair2 = moves.get(j);
                if (pair.getFirst().equals(pair2.getSecond())) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                //a<-b
                //c<-a
                //TODO处理方式可以优化
                ObjOperand res = parseOperand(pair.getFirst(), opMap);
                tmp.put(pair.getFirst(), new ObjVirReg());
                ObjOperand value = parseOperand(pair.getSecond(), opMap);
                currentBlock.addInstr(new ObjMove("move", tmp.get(pair.getFirst()), res));
                if (value instanceof ObjImm) {
                    currentBlock.addInstr(new ObjMove("li", res, value));
                } else {
                    currentBlock.addInstr(new ObjMove("move", res, value));
                }
            } else {
                ObjOperand res = parseOperand(pair.getFirst(), opMap);
                ObjOperand value;
                if (tmp.containsKey(pair.getSecond())) {
                    value = tmp.get(pair.getSecond());
                } else {
                    value = parseOperand(pair.getSecond(), opMap);
                }
                if (value instanceof ObjImm) {
                    currentBlock.addInstr(new ObjMove("li", res, value));
                } else {
                    currentBlock.addInstr(new ObjMove("move", res, value));
                }
            }
        }
    }

    public void parseRet(HashMap<Value, ObjReg> opMap, RetInstr retInstr) {
        if (currentFunction.getName().equals("main")) {
            currentBlock.addInstr(new ObjMove("li", ObjPhyReg.nameToReg.get("v0"), new ObjImm(10)));
            currentBlock.addInstr(new ObjSyscall());
            return;
        }
        if (retInstr.getValue() != null) {
            ObjOperand operand = parseOperand(retInstr.getValue(), opMap);
            if (operand instanceof ObjImm) {
                currentBlock.addInstr(new ObjMove("li", ObjPhyReg.nameToReg.get("v0"), operand));
            } else {
                currentBlock.addInstr(new ObjMove("move", ObjPhyReg.nameToReg.get("v0"), operand));
            }
        }
        if (irModule.functions.size() > 1) {
            currentBlock.addInstr(new ObjStore("lw", ObjPhyReg.RA, ObjPhyReg.SP, new ObjImm(currentFunction.argSize + currentFunction.regSize)));
        }
        currentBlock.addInstr(new ObjBinary("addi", ObjPhyReg.SP, ObjPhyReg.SP, new ObjImm(currentFunction.getStackSize())));
        currentBlock.addInstr(new ObjJ("jr", ObjPhyReg.RA));
    }

    public void parseStore(HashMap<Value, ObjReg> opMap, StoreInstr storeInstr) {
        ObjOperand value = parseOperand(storeInstr.getValue(), opMap);
        ObjOperand addr = parseOperand(storeInstr.getAddr(), opMap);
        if (value instanceof ObjImm objImm) {
            value = new ObjVirReg();
            currentBlock.addInstr(new ObjMove("li", value, objImm));
        }
        if (storeInstr.getAddr().getType().equals(new PointerType(new IntegerType(8)))) {
            currentBlock.addInstr(new ObjStore("sb", value, addr, new ObjImm(0)));
        } else {
            currentBlock.addInstr(new ObjStore("sw", value, addr, new ObjImm(0)));
        }
    }

    public void parseChangeType(HashMap<Value, ObjReg> opMap, Instruction instr) {
        ObjOperand res, val;
        boolean flag = false;
        if (instr instanceof ZextInstr zextInstr) {
            res = parseOperand(zextInstr.getRes(), opMap);
            val = parseOperand(zextInstr.getVal(), opMap);
            if (zextInstr.getRes().getType().equals(new IntegerType(8))) {
                flag = true;
            }
        } else {
            TruncInstr truncInstr = (TruncInstr) instr;
            res = parseOperand(truncInstr.getRes(), opMap);
            val = parseOperand(truncInstr.getVal(), opMap);
            if (truncInstr.getRes().getType().equals(new IntegerType(8))) {
                flag = true;
            }
        }
        if (flag) {
            if (val instanceof ObjImm imm) {
                currentBlock.addInstr(new ObjMove("li", res, new ObjImm(imm.getImmediate() & 255)));
            } else {
                currentBlock.addInstr(new ObjBinary("andi", res, val, new ObjImm(255)));
            }
        } else {
            //TODO存疑
            if (val instanceof ObjImm imm) {
                currentBlock.addInstr(new ObjMove("li", res, val));
            } else {
                currentBlock.addInstr(new ObjMove("move", res, val));
            }
        }
    }
}
/*
当前栈帧sp，函数A调用B
调用前：
----高地址
alloca的变量
...
ra
s0
...
s7
....
arg1(sp+4)
arg0(sp)
----低地址
ra为A的ra，arg为传入B的参数

通过遍历指令，确定需要alloca的大小、最多预留多少参数空间
函数A调用B时，保存ra,s0-s8,传递参数
函数B开头通过以上类似过程确定大小+ra+s0-s7申请自己的栈空间，即sp=sp-size
随后取出A传递的参数
返回时销毁栈帧
*/