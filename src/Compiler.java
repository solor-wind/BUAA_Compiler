import frontend.lexer.Lexer;

import java.io.FileWriter;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer("testfile.txt");
        lexer.scan();
        if (lexer.isError()) {
            FileWriter writer = new FileWriter("error.txt");
            writer.write(lexer.toString());
            writer.close();
        } else {
            FileWriter writer = new FileWriter("lexer.txt");
            writer.write(lexer.toString());
            writer.close();
        }

    }
}
