package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.List;

/**
 * Pattern node that matches constructor-shaped messages and recursively checks each field.
 */
public class ConstructorFormat extends Format {
    String name;
    List<Format> args;

    public ConstructorFormat(String text, List<Format> args) {
        this.name = text;
        this.args = args;
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel label) {
        FormatType formatType = delta.getFormat(name);

        if (args.size() != formatType.fields.size()) {
            throw new RuntimeException("Wrong number of fields in format pattern " + name);
        }

        for (int i = 0; i < args.size(); i++) {
            Param expected = formatType.fields.get(i);
            Format actual = args.get(i);

            if (actual instanceof TypedVarFormat typedVarFormat) {
                if (typedVarFormat.type != null && !Operators.sameType(typedVarFormat.type, expected.type)) {
                    throw new RuntimeException("Format pattern type mismatch in " + name);
                }
                if (typedVarFormat.label != expected.label) {
                    throw new RuntimeException("Format pattern label mismatch in " + name);
                }
            } else if (actual instanceof ConstructorFormat constructorFormat) {
                if (!Operators.sameType(expected.type, delta.getFormat(constructorFormat.name))) {
                    throw new RuntimeException("Nested format mismatch in " + name);
                }
            } else if (actual instanceof ExprFormat exprFormat) {
                Operators actualType = exprFormat.expr.typecheck(delta, gamma);
                if (!Operators.sameType(actualType, expected.type)) {
                    throw new RuntimeException("Format expression type mismatch in " + name);
                }
            }

            actual.typecheck(delta, gamma, label);
        }
    }

    @Override
    public String compile(CodeGenEnv env) {
        return "";
    }

    public boolean match(Value value, Environment env) {

        if (!(value instanceof ConstructorValue cv)) {
            return false;
        }

        if (!cv.name.equals(name)) {
            return false;
        }

        if (cv.values.size() != args.size()) {
            return false;
        }

        for (int i = 0; i < args.size(); i++) {
            if (!args.get(i).match(cv.values.get(i), env)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String compileMatch(CodeGenEnv env, String valueVar) {

        String cv = env.freshVar("cv");

        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("if (!(").append(valueVar)
                .append(" instanceof ConstructorValue)) throw new RuntimeException();\n");

        sb.append(env.indent())
                .append("ConstructorValue ").append(cv)
                .append(" = (ConstructorValue)").append(valueVar).append(";\n");

        sb.append(env.indent())
                .append("if (!").append(cv).append(".name.equals(\"")
                .append(name).append("\")) throw new RuntimeException();\n");

        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i).compileMatch(env, cv + ".values.get(" + i + ")"));
        }

        return sb.toString();
    }

    @Override
    public String describe() {
        return name + "(" + args.stream()
                .map(Format::describe)
                .reduce((left, right) -> left + ", " + right)
                .orElse("") + ")";
    }

    @Override
    public SecLabel label(LabelEnv gamma) {

        SecLabel result = SecLabel.LOW;

        for (Format arg : args) {
            result = SecLabel.join(result,
                    arg.label(gamma));
        }

        return result;
    }
}
