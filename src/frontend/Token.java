package frontend;

public class Token {
    private TokenType token;
    private String value;
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

    @Override
    public String toString() {
        if (token == TokenType.STRCON) {
            return token + " " + "\"" + value + "\"";
        } else if (token == TokenType.CHRCON) {
            return token + " " + "'" + value + "'";
        }
        return token + " " + value;
    }
}
