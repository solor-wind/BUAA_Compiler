package frontend.ast.units.exps;

import frontend.lexer.Token;

public class UnaryOp {
    private Token op;

    public UnaryOp(Token op) {
        this.op = op;
    }

    public Token getOp() {
        return op;
    }

    @Override
    public String toString() {
        return op + "\n<UnaryOp>";
    }
}
