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
     * Creates a receive statement with a pattern and a body executed on match.
     *
     * @param format Pattern that the next channel message must match.
     * @param body Statements executed only when matching succeeds.
     */
    public TryReceiveStmt(Format format, CmdBlock body) {
        this.format = format;
        this.body = body;
    }

    /**
     * Interprets the receive statement against the runtime inbox.
     *
     * <p>
     * Pattern-bound variables are declared with defaults before matching so
     * later statements can refer to them. The body is executed only when the
     * next message matches the expected encryption and format structure.
     * </p>
     *
     * @param env Runtime environment containing variables and the inbox.
     */
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

    /**
     * Declares default runtime values for variables introduced by the pattern.
     *
     * <p>
     * This mirrors the Java code generator, where receive-bound variables must
     * be definitely assigned even if the receive attempt fails.
     * </p>
     *
     * @param env Runtime environment to receive the default bindings.
     */
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

    /**
     * Type and label checks the receive pattern and its continuation.
     *
     * <p>
     * The receive body is checked in copied environments where pattern-bound
     * variables are available. Its procedure label is the join of the
     * incoming procedure and the pattern label.
     * </p>
     *
     * @param delta Type environment.
     * @param gamma Label environment.
     * @param label Current procedure label in a body.
     */
    @Override
    public void labelTypeChecker(TypeEnv delta, LabelEnv gamma, SecLabel label) {

        // Typecheck receive pattern using current procedure
        format.labelTypeCheck(delta, gamma, label);

        // New scope for body
        TypeEnv bodyDelta = new TypeEnv(delta);
        LabelEnv bodyGamma = new LabelEnv(gamma);

        // Compute overall label of received pattern
        SecLabel patternLabel = format.label(bodyGamma);

        // Takes the current with message label
        SecLabel newProcedureLabel = SecLabel.supremum(label, patternLabel);

        // IMPORTANT:
        // preserve current procedure
        body.labelTypeChecker(bodyDelta, bodyGamma, newProcedureLabel);
    }

    /**
     * Generates Java code for runtime receive-pattern matching.
     *
     * <p>
     * Variables bound by the pattern are declared before the generated
     * {@code try} block with Java defaults. If matching succeeds, those
     * variables are overwritten with received fields, the channel message is
     * removed, and the body is executed. If matching fails, the catch block is
     * empty and control continues with the defaults.
     * </p>
     *
     * @param env Code-generation environment.
     * @return Java source fragment for the receive statement.
     */
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

