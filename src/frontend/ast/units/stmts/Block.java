package frontend.ast.units.stmts;

import frontend.lexer.Token;

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
}
