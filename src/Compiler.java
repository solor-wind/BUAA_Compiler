import frontend.ast.Parser;
import frontend.lexer.Lexer;
import frontend.lexer.TokenStream;
import frontend.symbols.GetSymTable;

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
        GetSymTable getSymTable = new GetSymTable(parser.getCompUnit(), parser.getErrors());
        getSymTable.parse();
        if (!GetSymTable.isError()) {
            FileWriter writer = new FileWriter("symbol.txt");
            writer.write(GetSymTable.root.toString());
            writer.close();
        } else {
            FileWriter writer = new FileWriter("error.txt");
            writer.write(getSymTable.outError());
            writer.close();
        }
    }
}
