package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.Scanner;

/**
 * Statement node that reads a primitive value from standard input into an
 * existing variable.
 */
public class InputStmt extends Stmt {
    private static final Scanner INPUT = new Scanner(System.in);

    private final String name;

    public InputStmt(String name) {
        this.name = name;
    }

    @Override
    public void eval(Environment env) {
        Value current = env.getVariables(name);
        SecLabel label = env.getLabel(name);

        System.out.print("[INPUT] " + name + " = ");
        String text = INPUT.nextLine();

        Value value;
        if (current instanceof IntValue) {
            value = new IntValue(Integer.parseInt(text));
        } else if (current instanceof BoolValue) {
            value = new BoolValue(Boolean.parseBoolean(text));
        } else if (current instanceof StringValue) {
            value = new StringValue(text);
        } else {
            throw new RuntimeException("input only supports int, bool, and String variables: " + name);
        }

        value.label = label;
        env.setVariables(name, value);
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {
        Operators type = delta.getType(name);
        Type runtimeType = Operators.runtimeType(type);

        if (runtimeType != Type.INT && runtimeType != Type.BOOL && runtimeType != Type.STRING) {
            throw new TypeCheckException("input only supports int, bool, and String variables", lineNumber, name);
        }

        gamma.getLabel(name);
    }

    @Override
    public String compile(CodeGenEnv env) {
        Operators type = env.getVariableType(name);
        if (type == null) {
            throw new RuntimeException("Cannot generate input for undeclared variable: " + name);
        }

        String input = env.freshVar("input");
        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("System.out.print(\"[INPUT] ")
                .append(name)
                .append(" = \");\n");
        sb.append(env.indent())
                .append("String ")
                .append(input)
                .append(" = INPUT.nextLine();\n");
        sb.append(env.indent())
                .append(name)
                .append(" = ")
                .append(parseExpression(type, input))
                .append(";\n");

        return sb.toString();
    }

    private String parseExpression(Operators type, String input) {
        return switch (Operators.runtimeType(type)) {
            case INT -> "Integer.parseInt(" + input + ")";
            case BOOL -> "Boolean.parseBoolean(" + input + ")";
            case STRING -> input;
            case CIPHERTEXT, FORMAT -> throw new RuntimeException(
                    "input only supports int, bool, and String variables: " + name
            );
        };
    }
}
