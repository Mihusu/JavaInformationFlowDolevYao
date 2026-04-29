package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

public class SendStmt extends Stmt {
    String name;

    public SendStmt(String text) {
        this.name = text;
    }

    public void eval(Environment env) {
        env.outbox.add(env.getVariables(name));
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        if (delta.getType(name) == null) {
            throw new TypeCheckException("Undefined variable: " + name);
        }

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
}
