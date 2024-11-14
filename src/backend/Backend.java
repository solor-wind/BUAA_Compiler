package backend;

import backend.component.ObjModule;
import backend.process.LinearRegAlloc;
import backend.process.ParseIr;
import ir.value.IRModule;

import java.io.FileWriter;
import java.io.IOException;

public class Backend {
    private IRModule irModule;
    public ObjModule objModule;
    private ParseIr parseIr;
    private LinearRegAlloc linearRegAlloc;

    public Backend(IRModule irModule) {
        this.irModule = irModule;
        this.objModule = new ObjModule();
        this.parseIr = new ParseIr(irModule, objModule);
        linearRegAlloc = new LinearRegAlloc(objModule);
    }

    public void run() throws IOException {
        parseIr.parse();
        FileWriter writer = new FileWriter("vmips.txt");
        writer.write(objModule.toString());
        writer.close();
        linearRegAlloc.run();
    }
}
