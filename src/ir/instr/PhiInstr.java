package ir.instr;

import ir.value.BasicBlock;
import ir.value.Instruction;
import ir.value.Value;
import ir.value.Variable;
import utils.Pair;

import java.util.ArrayList;

public class PhiInstr extends Instruction {
    Variable res;
    ArrayList<Pair<Value, BasicBlock>> valLabel = new ArrayList<>();

    public PhiInstr(Variable res) {
        super("phi");
        this.res = res;
    }

    public Variable getRes() {
        return res;
    }

    public ArrayList<Pair<Value, BasicBlock>> getValLabel() {
        return valLabel;
    }

    public void addValLabel(Pair<Value, BasicBlock> pair) {
        valLabel.add(pair);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(res.getName()).append(" = phi ").append(res.getType());
        if (valLabel.isEmpty()) {
            return sb.toString();//应该永远不被执行
        }
        sb.append(" [ ").append(valLabel.get(0).getFirst().getName()).append(", %").append(valLabel.get(0).getSecond().getName()).append(" ]");
        for (int i = 1; i < valLabel.size(); i++) {
            sb.append(", [ ").append(valLabel.get(i).getFirst().getName()).append(", %").append(valLabel.get(i).getSecond().getName()).append(" ]");
        }
        return sb.toString();
    }
}
