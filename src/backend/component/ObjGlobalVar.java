package backend.component;

import ir.type.ArrayType;
import ir.type.IntegerType;
import ir.type.Type;

import java.util.ArrayList;


public class ObjGlobalVar {
    private String name;
    private Type type;
    private ArrayList<Integer> inits = new ArrayList<>();
    private String stringConst;

    public ObjGlobalVar(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public ObjGlobalVar(String name, Type type, ArrayList<Integer> inits) {
        this.name = name;
        this.type = type;
        this.inits = inits;
    }

    public void addInit(Integer i) {
        inits.add(i);
    }

    public void setInits(ArrayList<Integer> inits) {
        this.inits = inits;
    }

    public void setStringConst(String stringConst) {
        this.stringConst = stringConst;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(": ");

        if (stringConst != null) {
            sb.append(".asciiz ").append(stringConst);
            return sb.toString();
        }

        if (type instanceof ArrayType arrayType) {
            int length = arrayType.getLength();
            String s = ".word";
            if (((IntegerType) arrayType.getElementType()).getBits() == 8) {
                s = ".byte";
            }
            sb.append(s);
            if (inits.isEmpty()) {
                sb.append(" 0:").append(length);
                return sb.toString();
            }
            sb.append(" ").append(inits.get(0));
            for (int i = 1; i < length; i++) {
                if (i < inits.size()) {
                    sb.append(", ").append(inits.get(i));
                } else {
                    sb.append("\n\t").append(s).append(" 0:").append(length - inits.size());
                }
            }
        } else if (type instanceof IntegerType integerType) {
            if (integerType.getBits() == 32) {
                sb.append(".word ").append(inits.get(0));
            } else {
                sb.append(".byte ").append(inits.get(0));
            }
        }
        return sb.toString();

    }

    public String getName() {
        return name;
    }
}