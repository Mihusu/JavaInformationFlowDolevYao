package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.List;

/**
 * Pattern node that matches constructor-shaped messages and recursively checks each field.
 */
public class ConstructorPattern extends Format {
    String name;
    List<Format> args;

    public ConstructorPattern(String text, List<Format> args) {
        this.name = text;
        this.args = args;
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel label) {

        // The constructor name itself needs no checking for now.
        // Just recursively typecheck all subpatterns.

        for (Format p : args) {
            p.typecheck(delta, gamma, label);
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
    public SecLabel label(LabelEnv gamma) {

        SecLabel result = SecLabel.LOW;

        for (Format arg : args) {
            result = SecLabel.join(result,
                    arg.label(gamma));
        }

        return result;
    }
}
