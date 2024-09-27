package frontend.lexer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.LinkedList;

public class Lexer {
    private final LinkedList<Token> tokens;
    private final PushbackReader reader;
    private int line;

    private final LinkedList<Token> errors;

    public Lexer(String filename) throws FileNotFoundException {
        tokens = new LinkedList<>();
        reader = new PushbackReader(new FileReader(filename));
        line = 1;
        errors = new LinkedList<>();
    }

    public void scan() throws IOException {
        while (reader.ready()) {
            char c = (char) reader.read();
            if (Character.isDigit(c)) {
                reader.unread(c);
                scanNumber();
            } else if (Character.isLetter(c) || c == '_') {
                reader.unread(c);
                scanLetter();
            } else if (c == '\n') {
                line++;
            } else if (!Character.isWhitespace(c)) {
                reader.unread(c);
                scanSymbol();
            }
        }
        reader.close();
    }

    public void scanNumber() throws IOException {
        StringBuilder number = new StringBuilder();
        while (reader.ready()) {
            char num = (char) reader.read();
            if (Character.isDigit(num)) {
                number.append(num);
            } else {
                reader.unread(num);
                break;
            }
        }
        tokens.add(new Token(TokenType.INTCON, number.toString(), line));
    }

    public void scanLetter() throws IOException {
        StringBuilder letter = new StringBuilder();
        while (reader.ready()) {
            char ch = (char) reader.read();
            if (!(Character.isLetter(ch) || Character.isDigit(ch) || ch == '_')) {
                reader.unread(ch);
                break;
            }
            letter.append(ch);
        }
        String word = letter.toString();
        switch (word) {
            case "main" -> tokens.add(new Token(TokenType.MAINTK, word, line));
            case "const" -> tokens.add(new Token(TokenType.CONSTTK, word, line));
            case "int" -> tokens.add(new Token(TokenType.INTTK, word, line));
            case "char" -> tokens.add(new Token(TokenType.CHARTK, word, line));
            case "void" -> tokens.add(new Token(TokenType.VOIDTK, word, line));
            case "break" -> tokens.add(new Token(TokenType.BREAKTK, word, line));
            case "continue" -> tokens.add(new Token(TokenType.CONTINUETK, word, line));
            case "if" -> tokens.add(new Token(TokenType.IFTK, word, line));
            case "else" -> tokens.add(new Token(TokenType.ELSETK, word, line));
            case "for" -> tokens.add(new Token(TokenType.FORTK, word, line));
            case "getint" -> tokens.add(new Token(TokenType.GETINTTK, word, line));
            case "getchar" -> tokens.add(new Token(TokenType.GETCHARTK, word, line));
            case "printf" -> tokens.add(new Token(TokenType.PRINTFTK, word, line));
            case "return" -> tokens.add(new Token(TokenType.RETURNTK, word, line));
            default -> tokens.add(new Token(TokenType.IDENFR, word, line));
        }
    }

    public void scanSymbol() throws IOException {
        char ch = (char) reader.read();
        StringBuilder symbol = new StringBuilder();
        switch (ch) {
            case '\"':
                ch = (char) reader.read();//一定会匹配吗？?
                while (ch != '"') {
                    /*
                    词法分析作业中，要求将\t等转译字符原样输出，即要输出\和t
                    但这样做，实际的value将错误*/
                    if (ch == '\\') {
                        ch = scanTrans();
                    }
                    symbol.append(ch);
                    ch = (char) reader.read();
                }
                tokens.add(new Token(TokenType.STRCON, symbol.toString(), line));
                break;
            case '\'':
                ch = (char) reader.read();//一定会匹配吗？?
                while (ch != '\'') {
                    if (ch == '\\') {
                        ch = scanTrans();
                    }
                    symbol.append(ch);
                    ch = (char) reader.read();
                }
                tokens.add(new Token(TokenType.CHRCON, symbol.toString(), line));
                break;
            case '/':
                scanSlash();
                break;
            case '|':
                if (!reader.ready()) {
                    errors.add(new Token(TokenType.ERROR, "|", line));
                }
                ch = (char) reader.read();
                if (ch != '|') {
                    errors.add(new Token(TokenType.ERROR, "|", line));
                    reader.unread(ch);
                } else {
                    tokens.add(new Token(TokenType.OR, "||", line));
                }
                break;
            case '&':
                if (!reader.ready()) {
                    errors.add(new Token(TokenType.ERROR, "&", line));
                }
                ch = (char) reader.read();
                if (ch != '&') {
                    errors.add(new Token(TokenType.ERROR, "&", line));
                    reader.unread(ch);
                } else {
                    tokens.add(new Token(TokenType.AND, "&&", line));
                }
                break;
            default:
                reader.unread(ch);
                scanSingleSymbol();
        }
    }

    public char scanTrans() throws IOException {
        char ch = (char) reader.read();
        return switch (ch) {
            case 'a' -> 7;
            case 'b' -> 8;
            case 't' -> 9;
            case 'n' -> 10;
            case 'v' -> 11;
            case 'f' -> 12;
            case '\"' -> 34;
            case '\'' -> 39;
            case '\\' -> 92;
            default -> '\0';
        };
    }

    public void scanSlash() throws IOException {
        char ch = (char) reader.read();
        if (ch == '*') { //多行注释/**/
            while (reader.ready()) {
                ch = (char) reader.read();
                if (ch == '*') {
                    ch = (char) reader.read();
                    if (ch == '/') {
                        break;
                    } else if (ch == '\n') {
                        line++;
                    } else {
                        reader.unread(ch);
                    }
                } else if (ch == '\n') {
                    line++;
                }
            }
        } else if (ch == '/') { //单行注释
            while (reader.ready()) {
                ch = (char) reader.read();
                if (ch == '\n') {
                    line++;
                    break;
                }
            }
        } else {
            reader.unread(ch);
            tokens.add(new Token(TokenType.DIV, "/", line));
        }
    }

    public void scanSingleSymbol() throws IOException {
        char ch = (char) reader.read();
        switch (ch) {
            case '+' -> tokens.add(new Token(TokenType.PLUS, "+", line));
            case '-' -> tokens.add(new Token(TokenType.MINU, "-", line));
            case '*' -> tokens.add(new Token(TokenType.MULT, "*", line));
            case '%' -> tokens.add(new Token(TokenType.MOD, "%", line));
            case ';' -> tokens.add(new Token(TokenType.SEMICN, ";", line));
            case ',' -> tokens.add(new Token(TokenType.COMMA, ",", line));
            case '(' -> tokens.add(new Token(TokenType.LPARENT, "(", line));
            case ')' -> tokens.add(new Token(TokenType.RPARENT, ")", line));
            case '[' -> tokens.add(new Token(TokenType.LBRACK, "[", line));
            case ']' -> tokens.add(new Token(TokenType.RBRACK, "]", line));
            case '{' -> tokens.add(new Token(TokenType.LBRACE, "{", line));
            case '}' -> tokens.add(new Token(TokenType.RBRACE, "}", line));
            default -> {
                reader.unread(ch);
                scanDoubleSymbol();
            }
        }
    }

    public void scanDoubleSymbol() throws IOException {
        char ch = (char) reader.read();
        switch (ch) {
            case '!':
                if (!reader.ready()) {
                    tokens.add(new Token(TokenType.NOT, "!", line));
                }
                ch = (char) reader.read();
                if (ch == '=') {
                    tokens.add(new Token(TokenType.NEQ, "!=", line));
                } else {
                    tokens.add(new Token(TokenType.NOT, "!", line));
                    reader.unread(ch);
                }
                break;
            case '=':
                if (!reader.ready()) {
                    tokens.add(new Token(TokenType.ASSIGN, "=", line));
                }
                ch = (char) reader.read();
                if (ch == '=') {
                    tokens.add(new Token(TokenType.EQL, "==", line));
                } else {
                    tokens.add(new Token(TokenType.ASSIGN, "=", line));
                    reader.unread(ch);
                }
                break;
            case '<':
                if (!reader.ready()) {
                    tokens.add(new Token(TokenType.LSS, "<", line));
                }
                ch = (char) reader.read();
                if (ch == '=') {
                    tokens.add(new Token(TokenType.LEQ, "<=", line));
                } else {
                    tokens.add(new Token(TokenType.LSS, "<", line));
                    reader.unread(ch);
                }
                break;
            case '>':
                if (!reader.ready()) {
                    tokens.add(new Token(TokenType.GRE, ">", line));
                }
                ch = (char) reader.read();
                if (ch == '=') {
                    tokens.add(new Token(TokenType.GEQ, ">=", line));
                } else {
                    tokens.add(new Token(TokenType.GRE, ">", line));
                    reader.unread(ch);
                }
                break;
        }
    }

    public boolean isError() {
        return !errors.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        if (errors.isEmpty()) {
            for (Token token : tokens) {
                ans.append(token.toString() + "\n");
            }
        } else {
            for (Token token : errors) {
                ans.append(token.getLine() + " a\n");
            }
        }
        return ans.toString();
    }
}
