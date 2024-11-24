package ir.instr;

import ir.value.Instruction;
import ir.value.Value;
import utils.Pair;

import java.util.ArrayList;

public class PCInstr extends Instruction {
    ArrayList<Pair<Value, Value>> moves = new ArrayList<>();

    public PCInstr() {
        super("PC");
    }

    public ArrayList<Pair<Value, Value>> getMoves() {
        return moves;
    }

    public void addMove(Value res, Value value) {
        moves.add(new Pair<>(res, value));
    }
}
