import frontend.ast.Parser;
import frontend.lexer.Lexer;
import frontend.lexer.TokenStream;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer("testfile.txt");
        lexer.scan();
        TokenStream tokenStream = new TokenStream(new ArrayList<>(lexer.getTokens()));
        Parser parser = new Parser(tokenStream);
        parser.parse();
        if (parser.isError()) {
            FileWriter writer = new FileWriter("error.txt");
            writer.write(parser.toString());
            writer.close();
        } else {
            FileWriter writer = new FileWriter("parser.txt");
            writer.write(parser.toString());
            writer.close();
        }
    }
}
