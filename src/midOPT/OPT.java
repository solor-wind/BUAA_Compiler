package midOPT;

import ir.IRBuilder;
import ir.value.IRModule;

import java.io.FileWriter;
import java.io.IOException;

public class OPT {
    private IRModule irModule;
    private CFG cfg;
    private DomTree domTree;
    private Mem2Reg mem2Reg;

    public OPT(IRModule irModule) {
        this.irModule = irModule;
    }

    public void run() throws IOException {
        FileWriter writer = new FileWriter("llvm_ir_no_opt.txt");
        writer.write(IRBuilder.irModule.toString());
        writer.close();
        cfg = new CFG(irModule);
        cfg.run();
        cfg.deleteBlock();
        cfg.mergeBlock();
        cfg.run();
        writer = new FileWriter("llvm_ir_no_mem2reg.txt");
        writer.write(IRBuilder.irModule.toString());
        writer.close();
        domTree = new DomTree(irModule, cfg);
        domTree.run();
        mem2Reg = new Mem2Reg(irModule, domTree, cfg);
        mem2Reg.run();
    }
}
