package frontend.ast.units.stmts;

import frontend.lexer.Token;

public class CharConst {
    public Token token;

    public CharConst(Token token) {
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
