package ir;

import ir.type.IntegerType;
import ir.type.PointerType;
import ir.type.VoidType;
import ir.value.Argument;
import ir.value.BasicBlock;
import ir.value.Function;
import ir.value.IRModule;

import java.util.HashMap;

public class IRBuilder {
    public static int globalVarName = 0;
    public static int varName = 0;
    public static int blockName = 0;
    public static IRModule irModule;
    public static Function currentFunction;
    public static BasicBlock currentBlock;

    public static HashMap<String, Function> libFunctions = new HashMap<>();

    public static void initLibfunc() {
        libFunctions.put("getint", new Function("@getint", new IntegerType(32)));
        libFunctions.put("getchar", new Function("@getchar", new IntegerType(32)));

        Function putint = new Function("@putint", new VoidType());
        putint.addArgument(new Argument("a", new IntegerType(32)));
        libFunctions.put("putint", putint);

        Function putch = new Function("@putch", new VoidType());
        putch.addArgument(new Argument("a", new IntegerType(32)));
        libFunctions.put("putch", putch);

        Function putstr = new Function("@putstr", new VoidType());
        putstr.addArgument(new Argument("a", new PointerType(new IntegerType(8))));
        libFunctions.put("putstr", putstr);
    }

    public static Function getLibFunction(String name) {
        return libFunctions.get(name);
    }

    public static void buildIR() {

    }

    public static String getGlobalVarName() {
        return "@" + globalVarName++;
    }

    public static String getVarName() {
        return "%" + varName++;
    }

    public static String getBlockName() {
        return "b" + blockName++;
    }

    /**
     * 接上currentblock并修改内部链表关系，然后替换currentBlock
     */
    public static void nextBlock(BasicBlock block) {
        currentBlock.setNextBlock(block);
        block.setPreBlock(currentBlock);
        currentBlock = block;
    }

    public static void connectBlock(BasicBlock block1, BasicBlock block2) {
        block1.setNextBlock(block2);
        block2.setPreBlock(block1);
    }

    public static void insertBlock(BasicBlock block1, BasicBlock block2, BasicBlock toInsert) {
        block1.setNextBlock(toInsert);
        block2.setPreBlock(toInsert);
        toInsert.setPreBlock(block1);
        toInsert.setNextBlock(block2);
    }

    public static void nextFunction(Function function) {
        currentFunction.addBlock(currentBlock);
        currentFunction = function;
        currentBlock = new BasicBlock(getBlockName(), currentFunction);
    }

}
