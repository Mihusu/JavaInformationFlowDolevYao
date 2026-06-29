package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.LinkedHashMap;
import java.util.Map;

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
        declareDefaultBindings(env);
        if (env.inbox.isEmpty()) return;

        Value msg = env.inbox.peek();

        if (format.match(msg, env)) {
            env.inbox.poll();
            body.eval(env);
        }
    }

    private void declareDefaultBindings(Environment env) {
        Map<String, Types> bindings = new LinkedHashMap<>();
        Map<String, SecLabel> labels = new LinkedHashMap<>();
        format.collectBindings(bindings);
        format.collectBindingLabels(labels);

        for (Map.Entry<String, Types> binding : bindings.entrySet()) {
            String name = binding.getKey();
            Types type = binding.getValue();
            SecLabel bindingLabel = labels.getOrDefault(name, SecLabel.LOW);
            Value value = defaultRuntimeValue(type);
            value.label = bindingLabel;
            env.declare(name, value, bindingLabel);
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

        Map<String, Types> bindings = new LinkedHashMap<>();
        format.collectBindings(bindings);
        for (Map.Entry<String, Types> binding : bindings.entrySet()) {
            String name = binding.getKey();
            Types type = binding.getValue();
            if (!env.isVariableDeclaredInCurrentScope(name)) {
                env.declareVariable(name, type);
                sb.append(env.indent())
                        .append(javaType(type)).append(" ")
                        .append(name).append(" = ")
                        .append(defaultValue(type)).append(";\n");
            }
        }

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
        env.decreaseIndent();
        sb.append(env.indent()).append("} catch (Exception e) {\n");
        sb.append("    ").append(body.compile(env));
        sb.append(env.indent()).append("}\n");

        return sb.toString();
    }

    private String javaType(Types type) {
        return type == null ? "Object" : JavaTypeSupport.toJavaType(type);
    }

    private String defaultValue(Types type) {
        return type == null ? "null" : JavaTypeSupport.defaultValueExpression(type);
    }

    private Value defaultRuntimeValue(Types type) {
        return type == null ? new StringValue("") : JavaTypeSupport.defaultValue(type);
    }

    private String escapeJava(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
