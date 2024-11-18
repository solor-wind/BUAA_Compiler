package backend.operand;

import backend.component.ObjBlock;
import backend.component.ObjFunction;
import backend.component.ObjGlobalVar;

public class ObjLabel extends ObjOperand {
    private ObjBlock block;
    private ObjGlobalVar globalVar;
    private ObjFunction function;

    public ObjLabel(ObjBlock block) {
        super(block.getName());
        this.block = block;
    }

    public ObjLabel(ObjGlobalVar globalVar) {
        super(globalVar.getName());
        this.globalVar = globalVar;
    }

    public ObjLabel(ObjFunction function) {
        super(function.getName());
        this.function = function;
    }

    public ObjBlock getBlock() {
        return block;
    }

    public ObjFunction getFunction() {
        return function;
    }

    public String getName() {
        if (globalVar != null) {
            return globalVar.getName();
        } else if (function != null) {
            return function.getName();
        } else {
            return block.getName();
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
