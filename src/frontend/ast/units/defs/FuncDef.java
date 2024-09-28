package frontend.ast.units.defs;

import frontend.ast.units.stmts.Block;
import frontend.lexer.Token;

public class FuncDef implements Unit {
    private Token funcType;
    private Token funcName;
    private Token lparent;
    private Token rparent;
    private FuncFParams funcFParams = null;
    private Block block;

    public FuncDef(Token funcType, Token funcName) {
        this.funcType = funcType;
        this.funcName = funcName;
    }

    public void setLparent(Token lparent) {
        this.lparent = lparent;
    }

    public void setRparent(Token rparent) {
        this.rparent = rparent;
    }

    public void setFuncFParams(FuncFParams funcFParams) {
        this.funcFParams = funcFParams;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public String toString() {
        String para = "";
        if (funcFParams != null) {
            para = funcFParams.toString() + "\n";
        }
        String tmp = funcType + "\n";
        if (!funcName.getValue().equals("main")) {
            tmp += "<FuncType>\n";
        }
        return tmp +
                funcName + "\n" +
                lparent + "\n" +
                para +
                rparent + "\n" +
                block;
    }
}
