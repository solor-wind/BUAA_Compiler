package frontend.ast.units.stmts;

import frontend.ast.units.defs.ConstDecl;
import frontend.ast.units.defs.VarDecl;
import frontend.lexer.Token;
import frontend.symbols.GetSymTable;
import frontend.symbols.SymbolTable;

import java.util.LinkedList;

public class Block {
    private Token lbrace;
    private Token rbrace;
    private LinkedList<BlockItem> blockItems = new LinkedList<>();

    public void setLbrace(Token lbrace) {
        this.lbrace = lbrace;
    }

    public void setRbrace(Token rbrace) {
        this.rbrace = rbrace;
    }

    public void addBlockItem(BlockItem blockItem) {
        blockItems.addLast(blockItem);
    }

    public LinkedList<BlockItem> getBlockItems() {
        return blockItems;
    }

    public Token getRbrace() {
        return rbrace;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lbrace + "\n");
        for (BlockItem blockItem : blockItems) {
            sb.append(blockItem + "\n");
        }
        sb.append(rbrace + "\n");
        return sb.toString() + "<Block>";
    }

    public void checkError(SymbolTable symbolTable) {
        SymbolTable s = new SymbolTable(symbolTable, symbolTable.getFuncSym());
        s.setOutID(++GetSymTable.outID);
        for (BlockItem blockItem : blockItems) {
            if (blockItem instanceof Stmt stmt) {
                stmt.checkError(s);
            } else if (blockItem instanceof ConstDecl constDecl) {
                constDecl.checkError(s);
            } else if (blockItem instanceof VarDecl varDecl) {
                varDecl.checkError(s);
            }
        }
        symbolTable.addChild(s);
    }
}
