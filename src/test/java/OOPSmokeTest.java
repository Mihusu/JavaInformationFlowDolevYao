import ASTBuilder.ASTBuilder;
import ASTNodes.Program;
import Analysis.Environment;
import Analysis.TypeChecker;
import CodeGeneration.CodeGenEnv;
import antlr.Information_flowLexer;
import antlr.Information_flowParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OOPSmokeTest {
    @Test
    public void supportsInheritanceAndMemberSyntax() {
        String source = """
                class Parent {
                    int low x = 1;
                    public int low getX() {
                        return x;
                    }
                }

                class Child extends Parent {
                    int low y = 2;
                    public int low sum() {
                        return this.x + this.y;
                    }
                }

                class Main {
                    Child low c = new Child();
                    Parent low p = c;
                    int low z = c.sum();
                    int low a = p.getX();
                    print(z, a, c.x);
                }
                """;

        Information_flowLexer lexer = new Information_flowLexer(CharStreams.fromString(source));
        Information_flowParser parser = new Information_flowParser(new CommonTokenStream(lexer));
        Program program = (Program) new ASTBuilder().visit(parser.program());

        new TypeChecker().check(program);
        String generated = program.compile(new CodeGenEnv());
        program.eval(new Environment());

        assertTrue(generated.contains("class Child extends Parent"));
        assertTrue(generated.contains("c.sum()"));
        assertTrue(generated.contains("c.x"));
    }
}
