package frontend.ast.units.stmts;

import frontend.lexer.Token;

public class IntConst {
    public Token token;

    public IntConst(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        return token.toString();
    }
}
