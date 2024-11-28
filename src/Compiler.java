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

/*
putch 是i32 ? i8?
常量无需load,需要优化
if(0||1)应当直接给出结果
所有常量和全局变量在语义分析时必须evaluate完毕，其他变量没有初值
生成LLVM IR时，常量和全局变量直接引用语义分析符号中的初值，其他通过genIR解决
所有ident，均使用建立符号表时所生成的key，获取Variable也是
除数组外，求值均load
生成LLVM IR要干的事：
1.存储结构
    除module外，均继承Value
    module-function-block-instr
    module存储function、globalVar
    function存储参数、block
    block存储指令、前后block
2.生成IR
    各层次递归调用
    2.1关于变量
        有一个从语义分析符号表到LLVM IR value的映射
        临时变量%1
        局部变量设置dirty，重复赋值就需要alloc
        全局变量直接load,store——实质为指针
    2.2语句
TODO:printf、初值的强制类型转换、局部变量不用%1编号、代码块以及变量名的重排
*/
//全局变量、所有常量初始化
//float,struct,scanf,[][],++--+=-=,while
//删除未使用的局部变量、替换所有常量
//合并基本块合并得更彻底些
//数组长度为1的变量转成非数组类型（注意传参）
//函数参数按需取
//changetype、乘除法优化