package frontend.ast.units.defs;

import frontend.ast.units.stmts.BlockItem;
import frontend.lexer.Token;
import frontend.symbols.SymbolTable;
import ir.IRBuilder;
import ir.value.Function;
import ir.value.Value;
import ir.value.Variable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class VarDecl implements Decl, BlockItem {
    private Token BType;//int,char
    private LinkedList<VarDef> varDefs = new LinkedList<>();
    private LinkedList<Token> commas = new LinkedList<>();//,
    private Token semicn;//;

    public VarDecl(Token BType) {
        this.BType = BType;
        varDefs = new LinkedList<>();
    }

    public void addVarDef(VarDef varDef) {
        varDefs.add(varDef);
    }

    public void addCommas(Token comma) {
        commas.add(comma);
    }

    public void setSemicn(Token semicn) {
        this.semicn = semicn;
    }

    public Token getBType() {
        return BType;
    }

    public LinkedList<VarDef> getVarDefs() {
        return varDefs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(BType + "\n");
        Iterator<VarDef> it = varDefs.iterator();
        Iterator<Token> it2 = commas.iterator();
        while (it.hasNext()) {
            sb.append(it.next() + "\n");
            if (it2.hasNext()) {
                sb.append(it2.next() + "\n");
            }
        }
        sb.append(semicn + "\n");
        return sb.toString() + "<VarDecl>";
    }

    public void checkError(SymbolTable symbolTable) {
        for (VarDef varDef : varDefs) {
            varDef.checkError(symbolTable, BType);
        }
    }

    public ArrayList<Variable> genGlobalIR(){
        ArrayList<Variable> vars = new ArrayList<>();
        for(VarDef varDef : varDefs){
            vars.add(varDef.genGlobalIR(BType));
        }
        return vars;
    }

    public void genIR(Function function){
        for(VarDef varDef : varDefs){
            varDef.genIR(function,BType);
        }
    }
}
