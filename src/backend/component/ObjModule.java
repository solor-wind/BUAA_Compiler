package backend.component;

import java.util.ArrayList;

public class ObjModule {
    private ArrayList<ObjGlobalVar> globalVars;
    private ArrayList<ObjFunction> functions;

    public ObjModule() {
        globalVars = new ArrayList<>();
        functions = new ArrayList<>();
    }

    public ArrayList<ObjFunction> getFunctions() {
        return functions;
    }

    public ArrayList<ObjGlobalVar> getGlobalVars() {
        return globalVars;
    }

    public void addGlobalVar(ObjGlobalVar g) {
        globalVars.add(g);
    }

    public void setGlobalVars(ArrayList<ObjGlobalVar> globalVars) {
        this.globalVars = globalVars;
    }

    public void addFunction(ObjFunction f) {
        functions.add(f);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(".data:\n");
        for (ObjGlobalVar globalVar : globalVars) {
            sb.append("\t").append(globalVar).append("\n");
        }
        sb.append(".text:\nj main\n");
        for (ObjFunction function : functions) {
//            if (function.getName().equals("main")) {
//                sb.append(".global\tmain\n");
//            }
            sb.append(function).append("\n");
        }
        if (sb.toString().contains("@")) {
            throw new RuntimeException();
        }
        return sb.toString();
    }
}
