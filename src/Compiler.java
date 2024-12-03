import backend.Backend;
import frontend.ast.Parser;
import frontend.lexer.Lexer;
import frontend.lexer.TokenStream;
import frontend.symbols.GetSymTable;
import ir.IRBuilder;
import ir.value.IRModule;
import midOPT.OPT;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer("testfile.txt");
        lexer.scan();
        TokenStream tokenStream = new TokenStream(new ArrayList<>(lexer.getTokens()));
        Parser parser = new Parser(tokenStream);
        parser.parse();
        GetSymTable getSymTable = new GetSymTable(parser.getCompUnit(), parser.getErrors());
        getSymTable.parse();
        if (GetSymTable.isError()) {
            FileWriter writer = new FileWriter("error.txt");
            writer.write(getSymTable.outError());
            writer.close();
            return;
        }

        IRBuilder.irModule = new IRModule("procedure");
        IRBuilder.initLibfunc();
        parser.getCompUnit().genIR();

        FileWriter writer = new FileWriter("llvm_ir_no_opt.txt");
        writer.write(IRBuilder.irModule.toString());
        writer.close();

        OPT opt = new OPT(IRBuilder.irModule);
        opt.run();

        FileWriter writer3 = new FileWriter("llvm_ir_removePhi.txt");
        writer3.write(IRBuilder.irModule.toString());
        writer3.close();

        Backend backend = new Backend(IRBuilder.irModule);
        backend.run();
        FileWriter writer2 = new FileWriter("mips.txt");
        writer2.write(backend.objModule.toString());
        writer2.close();

    }
}

//float,struct,scanf,[][],++--+=-=,while
//数组长度为1的变量转成非数组类型（注意传参）