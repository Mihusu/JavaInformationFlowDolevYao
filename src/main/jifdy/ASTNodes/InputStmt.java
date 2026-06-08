package ASTNodes;

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
    private final FieldAccessExpr field;
    private Operators cachedType;

    public InputStmt(String name) {
        this.name = name;
        this.field = null;
    }

    public InputStmt(FieldAccessExpr field) {
        this.name = null;
        this.field = field;
    }

    @Override
    public void eval(Environment env) {
        Value current;
        SecLabel label;
        ObjectValue receiver = null;
        String targetName = name;

        if (field != null) {
            Value receiverValue = field.receiver.eval(env);
            if (!(receiverValue instanceof ObjectValue object)) {
                throw new RuntimeException("input receiver is not an object: " + field.fieldName);
            }
            receiver = object;
            current = env.getField(object, field.fieldName);
            label = object.fieldLabels.get(field.fieldName);
            targetName = field.compile(new CodeGenEnv());
        } else {
            current = env.getVariables(name);
            label = env.getLabel(name);
        }

        System.out.print("[INPUT] " + targetName + " = ");
        String text = INPUT.nextLine();

        Value value;
        if (current instanceof IntValue) {
            value = new IntValue(Integer.parseInt(text.trim()));
        } else if (current instanceof BoolValue) {
            value = new BoolValue(Boolean.parseBoolean(text.trim()));
        } else if (current instanceof StringValue) {
            value = new StringValue(text);
        } else {
            throw new RuntimeException("input only supports int, bool, and String variables: " + name);
        }

        value.label = label;
        if (receiver != null) {
            env.setField(receiver, field.fieldName, value);
        } else {
            env.setVariables(name, value);
        }
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel secLabel) {
        Operators type = field == null ? delta.getType(name) : field.typecheck(delta, gamma);
        cachedType = type;
        Type runtimeType = Operators.runtimeType(type);

        if (runtimeType != Type.INT && runtimeType != Type.BOOL && runtimeType != Type.STRING) {
            throw new TypeCheckException("input only supports int, bool, and String variables", lineNumber, name);
        }

        if (field == null) {
            gamma.getLabel(name);
        } else {
            field.label(gamma);
        }
    }

    @Override
    public String compile(CodeGenEnv env) {
        String target = field == null ? name : field.compile(env);
        Operators type = field == null ? env.getVariableType(name) : cachedType;
        if (type == null) {
            throw new RuntimeException("Cannot generate input for undeclared variable: " + target);
        }

        String input = env.freshVar("input");
        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("System.out.print(\"[INPUT] ")
                .append(target)
                .append(" = \");\n");
        sb.append(env.indent())
                .append("String ")
                .append(input)
                .append(" = INPUT.nextLine();\n");
        sb.append(env.indent())
                .append(target)
                .append(" = ")
                .append(parseExpression(type, input))
                .append(";\n");

        return sb.toString();
    }

    private String parseExpression(Operators type, String input) {
        return switch (Operators.runtimeType(type)) {
            case INT -> "Integer.parseInt(" + input + ".trim())";
            case BOOL -> "Boolean.parseBoolean(" + input + ".trim())";
            case STRING -> input;
            case CIPHERTEXT, FORMAT, OBJECT -> throw new RuntimeException(
                    "input only supports int, bool, and String variables: " + name
            );
        };
    }
}
