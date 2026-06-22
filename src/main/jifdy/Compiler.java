import ASTNodes.Program;
import ASTBuilder.ASTBuilder;
import Analysis.Environment;
import Utils.ThrowingErrorListener;
import Analysis.TypeChecker;

import CodeGeneration.CodeGenEnv;
import antlr.Information_flowLexer;
import antlr.Information_flowParser;

import org.antlr.v4.runtime.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Entry point for parsing, type checking, code generation, and interpretation
 * to compile an example from a JIFDY language file.
 */
public class Compiler {

    public static void main(String[] args) throws Exception {

        // 1. Read the input file
        String inputFile = args.length > 0
                ? args[0]
                : "src/test/resources/testfiles/legalInformationFlow/BankTransfer.jifdy";

        CharStream input = CharStreams.fromFileName(inputFile);

        // 2. Lexer
        Information_flowLexer lexer = new Information_flowLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 3. Parser
        Information_flowParser parser = new Information_flowParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);

        // 4. Parse tree
        Information_flowParser.ProgramContext tree = parser.program();

        // 5. AST building
        ASTBuilder builder = new ASTBuilder();
        Program program = (Program) builder.visit(tree);

        // 6. Type Checking
        TypeChecker checker = new TypeChecker();
        checker.check(program);

        // 7. Code Generation
        CodeGenEnv codeEnv = new CodeGenEnv();

        String generatedCode = program.compile(codeEnv);

        // 8. Write a Java file
        Path output = Path.of("src/main/jifdy/CodeGeneration/GeneratedProgram.java");
        Files.writeString(output, generatedCode);

        System.out.println("Generated Java file: " + output.toAbsolutePath());

        // 9. Runtime environment
        Environment env = new Environment();

        // 10. Execute
        program.eval(env);
    }
}
