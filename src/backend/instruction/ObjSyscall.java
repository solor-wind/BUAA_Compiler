package backend.instruction;

import backend.component.ObjInstr;

public class ObjSyscall extends ObjInstr {
    public ObjSyscall() {
        super("syscall");
    }

    @Override
    public String toString() {
        return "syscall";
    }
}
