import ASTnodes.Program;
import ASTvisitors.ASTBuilder;
import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeChecker;

import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;
import antlr.Information_flowLexer;
import antlr.Information_flowParser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.nio.file.Files;
import java.nio.file.Path;

public class Compiler {

    public static void main(String[] args) throws Exception {

        // 1. Read the input file
        CharStream input = CharStreams.fromFileName("src/main/jifdy/JifdyFiles/main.jifdy");

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

    private static class ThrowingErrorListener extends BaseErrorListener {
        private static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

    }
}
