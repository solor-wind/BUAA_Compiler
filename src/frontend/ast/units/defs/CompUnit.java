package frontend.ast.units.defs;

import java.util.Iterator;
import java.util.LinkedList;

public class CompUnit implements Unit {
    private LinkedList<Decl> decls;
    private LinkedList<FuncDef> funcDefs;//最后一个是Main

    public CompUnit() {
        decls = new LinkedList<>();
        funcDefs = new LinkedList<>();
    }

    public void addDecl(Decl decl) {
        decls.add(decl);
    }

    public void addFuncDef(FuncDef funcDef) {
        funcDefs.add(funcDef);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Decl decl : decls) {
            sb.append(decl.toString()+"\n");
        }
        Iterator<FuncDef> it = funcDefs.iterator();
        sb.append(it.next().toString());
        while (it.hasNext()) {
            sb.append("\n<FuncDef>\n" + it.next().toString());
        }
        return sb + "\n<MainFuncDef>\n<CompUnit>";
    }
}
