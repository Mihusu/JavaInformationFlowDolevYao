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
    public String resultVar;
    public CmdBlock body;

    public TryReceiveStmt(Format format, CmdBlock body) {
        this(format, null, body);
    }

    public TryReceiveStmt(Format format, String resultVar, CmdBlock body) {
        this.format = format;
        this.resultVar = resultVar;
        this.body = body;
    }

    public void eval(Environment env) {
        System.out.println("[JIFDY] network -> TRY_RCV: " + format.describe());
        if (env.inbox.isEmpty()) return;

        Value msg = env.inbox.peek();

        if (format.match(msg, env)) {
            env.inbox.poll();
            if (resultVar != null) {
                env.setVariables(resultVar, msg);
            }
            body.eval(env);
        }
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel label) {

        // Typecheck receive pattern using current procedure
        format.typecheck(delta, gamma, label);

        // Optional ciphertext binding
        if (resultVar != null) {
            if (format instanceof EncryptFormat encryptFormat) {
                delta.putType(resultVar, encryptFormat.ciphertextType());
            } else {
                delta.putType(resultVar, Type.CIPHERTEXT);
            }
            gamma.putLabel(resultVar, SecLabel.LOW);
        }

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

        sb.append(format.compileMatch(env, msg));
        sb.append(env.indent()).append("channel.remove();\n");

        if (resultVar != null) {
            if (env.isVariableDeclared(resultVar)) {
                sb.append(env.indent()).append(resultVar).append(" = ").append(msg).append(";\n");
            } else {
                env.declareVariable(resultVar);
                sb.append(env.indent()).append("Object ").append(resultVar).append(" = ").append(msg).append(";\n");
            }
        }

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
