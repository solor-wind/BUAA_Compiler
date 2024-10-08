package frontend.lexer;

import java.util.ArrayList;

public class TokenStream {
    private final ArrayList<Token> tokens;
    private int pos = 0;


    public TokenStream(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public Token next() {
        if (pos < tokens.size()) {
            return tokens.get(pos++);
        }
        return null;
    }

    public Token peek() {
        if (pos >= tokens.size()) {
            return null;
        }
        return tokens.get(pos);
    }

    public Token peek(int i) {
        if (pos + i >= tokens.size() || pos + i < 0) {
            return null;
        }
        return tokens.get(pos + i);
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
