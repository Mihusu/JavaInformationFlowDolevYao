package Analysis;

import ASTnodes.Program;
import ASTvisitors.ASTBuilder;
import antlr.Information_flowLexer;
import antlr.Information_flowParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;

/**
 * Regression test suite for the JIFDY compiler/type checker.

 * PURPOSE:
 * --------
 * This class automatically tests all legal and illegal JIFDY programs.

 * The regression suite ensures that:

 * 1. Legal programs successfully pass parsing and type checking.
 * 2. Illegal programs correctly throw TypeCheckException.
 * 3. Future compiler changes do not break existing behavior.

 * The tests are divided into:

 * - LegalInformationFlow/
 *      Programs that SHOULD compile successfully.

 * - IlegalInformationFlow/
 *      Programs that SHOULD fail with security/type errors.

 * This acts as an automated validation framework for the
 * information flow type system.
 */
/**
 * Provides utility for running regression tests on the compiler.
 * Iterates through test files and verifies expected compilation/execution outcomes.
 */
public class RegressionTest {

    public static void main(String[] args) {
        String testDirGood = "src/test/resources/testfiles/LegalInformationFlow/";
        String testDirBad = "src/test/resources/testfiles/IlegalInformationFlow/";

        File goodFolder = new File(testDirGood);
        File badFolder = new File(testDirBad);

        File[] goodFiles = goodFolder.listFiles((dir, name) -> name.endsWith(".jifdy"));
        File[] badFiles = badFolder.listFiles((dir, name) -> name.endsWith(".jifdy"));

        int passed = 0;
        int failed = 0;

        System.out.println("Running Regression Tests...\n");

        if (goodFiles != null) {
            for (File f : goodFiles) {
                if (runTest(f.getPath(), true)) passed++;
                else failed++;
            }
        }

        if (badFiles != null) {
            for (File f : badFiles) {
                if (runTest(f.getPath(), false)) passed++;
                else failed++;
            }
        }

        System.out.println("\n----------------------------");
        System.out.println("Tests Passed: " + passed);
        System.out.println("Tests Failed: " + failed);
        System.out.println("----------------------------");

        if (failed > 0) {
            System.exit(1);
        }
    }

    /**
     * Runs a single JIFDY test file.

     * PARAMETERS:
     * -----------
     * filePath:
     *      Path to the .jifdy file.

     * shouldPass:
     *      true -> program should compile successfully
     *      false -> program should throw TypeCheckException

     * PIPELINE:
     * ---------
     * 1. Read the source file
     * 2. Lexical analysis (ANTLR lexer)
     * 3. Parsing (ANTLR parser)
     * 4. AST construction
     * 5. Type checking / IFC analysis
     * 6. Validate an expected result

     * RETURNS:
     * --------
     * true -> test behaved as expected
     * false -> unexpected behavior occurred
     */

    private static boolean runTest(String filePath, boolean shouldPass) {

        File file = new File(filePath);

        // fallback location
        if (!file.exists()) {

            String fileName = file.getName();

            filePath = "src/main/jifdy/JifdyFiles/Test/" + fileName;

            file = new File(filePath);

            if (!file.exists()) {
                System.out.println("FAILED (File not found: " + filePath + ")");
                return false;
            }
        }

        System.out.print("Testing " + filePath + " ... ");

        try {

            Information_flowLexer lexer =
                    new Information_flowLexer(
                            CharStreams.fromFileName(filePath)
                    );

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            Information_flowParser parser =
                    new Information_flowParser(tokens);

            Information_flowParser.ProgramContext tree =
                    parser.program();

            ASTBuilder builder = new ASTBuilder();

            Program program =
                    (Program) builder.visit(tree);

            TypeChecker checker = new TypeChecker();

            checker.check(program);

            if (shouldPass) {

                System.out.println("PASSED");
                return true;

            } else {

                System.out.println(
                        "FAILED (Expected TypeCheckException but it passed)"
                );

                return false;
            }

        } catch (TypeCheckException e) {

            if (!shouldPass) {

                System.out.println(
                        "PASSED (Caught expected exception: "
                                + e.getMessage() + ")"
                );

                return true;

            } else {

                System.out.println(
                        "FAILED (Unexpected TypeCheckException: "
                                + e.getMessage() + ")"
                );

                return false;
            }

        } catch (Exception e) {

            System.out.println(
                    "FAILED (Unexpected exception: "
                            + e.getClass().getSimpleName()
                            + ": "
                            + e.getMessage()
                            + ")"
            );

            e.printStackTrace();

            return false;
        }
    }
}
