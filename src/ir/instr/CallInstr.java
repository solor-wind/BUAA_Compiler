package ir.instr;

import ir.value.*;

import java.util.ArrayList;

public class CallInstr extends Instruction {
    private Variable res;
    private Function function;
    private ArrayList<Argument> arguments = new ArrayList<>();

    public CallInstr(Variable res, Function function, ArrayList<Argument> arguments) {
        super("call");
        this.res = res;
        this.function = function;
        this.arguments = arguments;
        for (int i = 0; i < arguments.size(); i++) {
            if (!function.getArguments().get(i).getType().equals(arguments.get(i).getType())) {
                throw new RuntimeException();
            }
        }
    }

    public CallInstr(Variable res, Function function, Value value) {
        super("call");
        this.res = res;
        this.function = function;
        this.arguments.add(new Argument(value));
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    public Function getFunction() {
        return function;
    }

    public Variable getRes() {
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Argument arg : arguments) {
            sb.append(arg.toString()).append(", ");
        }
        if (!sb.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }
        if (function.getType().is("void")) {
            return "call " + function.getType() + " " + function.getName() + "(" + sb + ")";
        }
        return res.getName() + " = call " + function.getType() + " " + function.getName() +
                "(" + sb.toString() + ")";
    }
}
