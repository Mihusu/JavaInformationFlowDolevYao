package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Utils.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Represents a send statement that transmits data over a communication channel.
 */
public class SendStmt extends Stmt {
    String name;

    public SendStmt(String text) {
        this.name = text;
    }

    public void eval(Environment env) {
        Value message = env.getVariables(name);
        System.out.println("[JIFDY] SEND -> network: " + describe(message));
        env.outbox.add(message);
    }

    @Override
    public void labelTypeChecker(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        delta.getType(name);

        // Optional (depending on your security model):
        // sending might leak → enforce LOW
        SecLabel l = gamma.getLabel(name);

        if (l == SecLabel.HIGH) {
            throw new TypeCheckException(
                    "Cannot send HIGH data: " + name
            );
        }
    }

    @Override
    public String compile(CodeGenEnv env) {
        return env.indent() +
                "channel.send(" + name + ");\n";
    }

    private String describe(Value value) {
        if (value instanceof EncryptedValue encryptedValue) {
            return "e(" + encryptedValue.key + ", " + describe(encryptedValue.payload) + ")";
        }

        if (value instanceof ConstructorValue constructorValue) {
            return constructorValue.name + "(...)";
        }

        return value.toString();
    }
}
