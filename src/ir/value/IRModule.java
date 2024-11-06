package ir.value;

import ir.type.ArrayType;
import ir.type.IntegerType;

import java.util.ArrayList;
import java.util.HashMap;

public class IRModule {
    public String name;
    public ArrayList<Function> functions = new ArrayList<>();
    public ArrayList<Variable> globalVariables = new ArrayList<>();
    public ArrayList<Constant> globalConstants = new ArrayList<>();
    private HashMap<String, Function> functionMap = new HashMap<>();//语义分析符号表到函数的映射
    private HashMap<String, Variable> globalVariableMap = new HashMap<>();//语义分析符号表到全局变量的映射

    public IRModule(String name) {
        this.name = name;
    }

    public void addFunction(Function f) {
        functions.add(f);
    }

    public void addFunction(String key, Function f) {
        functionMap.put(key, f);
    }

    public Function getFunction(String key) {
        return functionMap.get(key);
    }

    public void addGlobalVariable(Variable v) {
        globalVariables.add(v);
    }

    public void addGlobalVariable(String key, Variable v) {
        globalVariableMap.put(key, v);
    }

    public Variable getGlobalVariable(String key) {
        return globalVariableMap.get(key);
    }

    public void addGlobalVariables(ArrayList<Variable> v) {
        globalVariables.addAll(v);
    }

    public void addGlobalConstant(Constant c) {
        globalConstants.add(c);
    }

    public String declareGlobal() {
        StringBuilder sb = new StringBuilder();
        for (Variable var : globalVariables) {
            String ans = var.getName() + " = ";
            if (var.isConstant()) {
                ans += "constant ";
            } else {
                ans += "global ";
            }
            ans += var.getType();
            if (var.getType() instanceof ArrayType && var.getStringConst() != null) {
                ans += " " + var.getStringConst();
            } else if (var.getType() instanceof ArrayType arrayType) {
                ArrayList<Integer> inits = (ArrayList<Integer>) (var.getInitValue());
                String eleType = arrayType.getElementType().toString();
                if (inits.isEmpty() || (inits.size() == 1 && inits.get(0) == 0)) {
                    ans += " zeroinitializer";
                } else {
                    ans += " [";
                    ans += eleType + " " + inits.get(0);
                    for (int i = 1; i < inits.size(); i++) {
                        ans += ", " + eleType + " " + inits.get(i);
                    }
                    for (int i = inits.size(); i < arrayType.getLength(); i++) {
                        ans += ", " + eleType + " 0";
                    }
                    ans += "]";
                }
            } else {
                ans += " " + var.getInitValue();
            }
            sb.append(ans).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("declare i32 @getint()\n" +
                "declare i32 @getchar()\n" +
                "declare void @putint(i32)\n" +
                "declare void @putch(i32)\n" +
                "declare void @putstr(i8*)\n");
        sb.append(declareGlobal()).append("\n");
        for (Function f : functions) {
            sb.append(f.toString()).append("\n");
        }
        return sb.toString();
    }
}
