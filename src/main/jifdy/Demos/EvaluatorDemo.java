package Demos;

//import ASTvisitors.Evaluator;
import ASTnodes.Node;
import ASTvisitors.ASTBuilder;
import antlr.Information_flowLexer;
import antlr.Information_flowParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class EvaluatorDemo {

    public static void main(String[] args) {

        CharStream input;
        Information_flowLexer lexer;
        CommonTokenStream tokens;
        Information_flowParser parser;

        input = CharStreams.fromString("if(true){if(false){}else{bool asd = true;}}else{ bool eqw = false;}");

        lexer   = new Information_flowLexer(input);
        tokens  = new CommonTokenStream(lexer);
        parser  = new Information_flowParser(tokens);

        // Information_flowParser.IntegerDeclarationContext cst = parser.integerDeclaration();
        // ASTNode ast = new ASTbuilder().visitIntegerDeclaration(cst);

        Information_flowParser.IfStatementContext cst = parser.ifStatement();
        Node ast = new ASTBuilder().visitIfStatement(cst);

//        Evaluator evaluator = new Evaluator();
//        Object result = ast.accept(evaluator);
//
//        System.out.println(result);
    }
}
