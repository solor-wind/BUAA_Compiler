package frontend.lexer;

public class Token {
    private TokenType token;
    private String value;
    private String lex;
    private int line;

    public Token(TokenType token, String value, int line) {
        this.token = token;
        this.value = value;
        this.line = line;
    }

    public TokenType getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public void toLex() {
        lex = value.replace(Character.toString(92), "\\\\");
        lex = lex.replace(Character.toString(7), "\\a");
        lex = lex.replace(Character.toString(8), "\\b");
        lex = lex.replace(Character.toString(9), "\\t");
        lex = lex.replace(Character.toString(10), "\\n");
        lex = lex.replace(Character.toString(11), "\\v");
        lex = lex.replace(Character.toString(12), "\\f");
        lex = lex.replace(Character.toString(34), "\\\"");
        lex = lex.replace(Character.toString(39), "\\'");
        lex = lex.replace(Character.toString(0), "\\0");
    }

    @Override
    public String toString() {
        toLex();
        if (token == TokenType.STRCON) {
            return token + " " + "\"" + lex + "\"";
        } else if (token == TokenType.CHRCON) {
            return token + " " + "'" + lex + "'";
        }
        return token + " " + lex;
    }

    public boolean is(TokenType type) {
        return token == type;
    }
}
