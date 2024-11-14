package backend.operand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ObjPhyReg extends ObjReg {
    private final int index;
    private final String name;

    public final static HashMap<String, Integer> nameToIndex = new HashMap<>();
    public final static HashMap<Integer, String> indexToName = new HashMap<>();
    public final static ArrayList<ObjPhyReg> regs = new ArrayList<>();
    public final static HashMap<String, ObjPhyReg> nameToReg = new HashMap<>();
    private boolean isAllocated;

    public ObjPhyReg(String name) {
        super(name);
        this.name = name;
        this.index = nameToIndex.get(name);
        this.color = this.index;
    }

    public ObjPhyReg(int index) {
        super(indexToName.get(index));
        this.index = index;
        this.name = indexToName.get(index);
        this.color = this.index;
    }

    public ObjPhyReg(int index, boolean isAllocated) {
        this.name = indexToName.get(index);
        this.index = index;
        this.isAllocated = isAllocated;
        this.color = this.index;
    }

    public void setAllocated(boolean isAllocated) {
        this.isAllocated = isAllocated;
    }

    public int getIndex() {
        return index;
    }

    static {
        indexToName.put(0, "zero");
        indexToName.put(1, "at");
        indexToName.put(2, "v0");
        indexToName.put(3, "v1");
        for (int i = 4; i < 8; i++) {
            indexToName.put(i, "a" + (i - 4));
        }
        for (int i = 8; i < 16; i++) {
            indexToName.put(i, "t" + (i - 8));
        }
        for (int i = 16; i < 24; i++) {
            indexToName.put(i, "s" + (i - 16));
        }
        indexToName.put(24, "t8");
        indexToName.put(25, "t9");
        indexToName.put(26, "k0");
        indexToName.put(27, "k1");
        indexToName.put(28, "gp");
        indexToName.put(29, "sp");
        indexToName.put(30, "fp");
        indexToName.put(31, "ra");

        for (Map.Entry<Integer, String> entry : indexToName.entrySet()) {
            nameToIndex.put(entry.getValue(), entry.getKey());
        }
    }

    public final static ObjPhyReg ZERO = new ObjPhyReg("zero");
    public final static ObjPhyReg SP = new ObjPhyReg("sp");
    public final static ObjPhyReg RA = new ObjPhyReg("ra");

    static {
        regs.add(ZERO);
        regs.add(new ObjPhyReg("at"));
        regs.add(new ObjPhyReg("v0"));
        regs.add(new ObjPhyReg("v1"));
        for (int i = 0; i <= 3; i++) {
            ObjPhyReg opr = new ObjPhyReg("a" + i);
            regs.add(opr);
        }
        for (int i = 0; i <= 7; i++) {
            ObjPhyReg opr = new ObjPhyReg("t" + i);
            regs.add(opr);
        }
        for (int i = 0; i <= 7; i++) {
            ObjPhyReg opr = new ObjPhyReg("s" + i);
            regs.add(opr);
        }
        regs.add(new ObjPhyReg("t8"));
        regs.add(new ObjPhyReg("t9"));
        regs.add(new ObjPhyReg("k0"));
        regs.add(new ObjPhyReg("k1"));
        regs.add(new ObjPhyReg("gp"));
        regs.add(SP);
        regs.add(new ObjPhyReg("fp"));
        regs.add(RA);

        for (int i = 0; i < 32; i++) {
            nameToReg.put(indexToName.get(i), regs.get(i));
        }
    }

    @Override
    public String toString() {
        return "$" + name;
    }
}
