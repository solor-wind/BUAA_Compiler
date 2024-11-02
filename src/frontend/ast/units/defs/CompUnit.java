package frontend.ast.units.defs;

import frontend.symbols.SymbolTable;
import ir.IRBuilder;
import ir.value.Function;
import ir.value.IRModule;

import java.util.Iterator;
import java.util.LinkedList;

public class CompUnit implements Unit {
    private LinkedList<Decl> decls;
    private LinkedList<FuncDef> funcDefs;//最后一个是Main
    private LinkedList<Object> decfs;

    public CompUnit() {
        decls = new LinkedList<>();
        funcDefs = new LinkedList<>();
        decfs = new LinkedList<>();
    }

    public void addDecl(Decl decl) {
        decls.add(decl);
        decfs.add(decl);
    }

    public void addFuncDef(FuncDef funcDef) {
        funcDefs.add(funcDef);
        decfs.add(funcDef);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Decl decl : decls) {
            sb.append(decl.toString() + "\n");
        }
        Iterator<FuncDef> it = funcDefs.iterator();
        sb.append(it.next().toString());
        while (it.hasNext()) {
            sb.append("\n<FuncDef>\n" + it.next().toString());
        }
        return sb + "\n<MainFuncDef>\n<CompUnit>";
    }

    public void checkError(SymbolTable symbolTable) {
        for (Object object : decfs) {
            if (object instanceof FuncDef funcDef) {
                funcDef.checkError(symbolTable);
            } else if (object instanceof ConstDecl constDecl) {
                constDecl.checkError(symbolTable);
            } else if (object instanceof VarDecl varDecl) {
                varDecl.checkError(symbolTable);
            }
        }
        this.symbolTable = symbolTable;
    }

    private SymbolTable symbolTable;

    public void genIR() {
        for (Object object : decfs) {
            if (object instanceof FuncDef funcDef) {
                Function function = funcDef.genIR();
                function.addBlock(IRBuilder.currentBlock);
                IRBuilder.irModule.addFunction(function);
                IRBuilder.varName = 0;
            } else if (object instanceof ConstDecl constDecl) {
                IRBuilder.irModule.addGlobalVariables(constDecl.genGlobalIR());
            } else if (object instanceof VarDecl varDecl) {
                IRBuilder.irModule.addGlobalVariables(varDecl.genGlobalIR());
            }
        }
    }
}
