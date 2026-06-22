package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Represents a statement that attempts to receive a message from a channel.
 * Uses pattern matching to validate the received message.
 */
public class TryReceiveStmt extends Stmt {
    public Format format;
    public CmdBlock body;

    /**
     * Creates a receive statement that stores the complete matched
     */
    public TryReceiveStmt(Format format, CmdBlock body) {
        this.format = format;
        this.body = body;
    }

    public void eval(Environment env) {
        System.out.println("[JIFDY] network -> TRY_RCV: " + format.describe());
        if (env.inbox.isEmpty()) return;

        Value msg = env.inbox.peek();

        if (format.match(msg, env)) {
            env.inbox.poll();
            body.eval(env);
        }
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel label) {

        // Typecheck receive pattern using current procedure
        format.typecheck(delta, gamma, label);

        // New scope for body
        TypeEnv bodyDelta = new TypeEnv(delta);
        LabelEnv bodyGamma = new LabelEnv(gamma);

        // Compute overall label of received pattern
        SecLabel patternLabel = format.label(bodyGamma);

        // Takes the current with message label
        SecLabel newProcedureLabel = SecLabel.join(label, patternLabel);

        // IMPORTANT:
        // preserve current procedure
        body.typecheck(bodyDelta, bodyGamma, newProcedureLabel);
    }

    @Override
    public String compile(CodeGenEnv env) {

        String msg = env.freshVar("msg");

        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("System.out.println(\"[JIFDY] network -> TRY_RCV: ")
                .append(escapeJava(format.describe()))
                .append("\");\n");
        sb.append(env.indent()).append("try {\n");
        env.increaseIndent();

        sb.append(env.indent())
                .append("Object ").append(msg)
                .append(" = channel.peek();\n");

        sb.append(format.compile(env, msg));
        sb.append(env.indent()).append("channel.remove();\n");

        sb.append(body.compile(env));

        env.decreaseIndent();
        sb.append(env.indent()).append("} catch (Exception e) {}\n");

        return sb.toString();
    }

    private String escapeJava(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
