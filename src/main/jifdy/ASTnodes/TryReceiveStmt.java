package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

public class TryReceiveStmt extends Stmt {
    public ReceivePattern receivePattern;
    public CmdBlock body;

    public TryReceiveStmt(ReceivePattern receivePattern, CmdBlock body) {
        this.receivePattern = receivePattern;
        this.body = body;
    }

    public void eval(Environment env) {
        if (env.inbox.isEmpty()) return;

        Value msg = env.inbox.poll();

        if (receivePattern.match(msg, env)) {
            body.eval(env);
        }
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel label) {

        // Create new scope (inherit outer env)
        TypeEnv localDelta = new TypeEnv(delta);
        LabelEnv localGamma = new LabelEnv(gamma);

        // Bind variables from receive pattern as HIGH
        receivePattern.typecheck(localDelta, localGamma, SecLabel.HIGH);

        // Enter HIGH pc (implicit flow!)
        SecLabel newLabel = SecLabel.HIGH;

        // Typecheck body under HIGH context
        body.typecheck(localDelta, localGamma, newLabel);
    }

    @Override
    public String compile(CodeGenEnv env) {

        String msg = env.freshVar("msg");

        StringBuilder sb = new StringBuilder();

        sb.append(env.indent()).append("try {\n");
        env.increaseIndent();

        sb.append(env.indent())
                .append("Object ").append(msg)
                .append(" = channel.receive();\n");

        sb.append(receivePattern.compileMatch(env, msg));

        sb.append(body.compile(env));

        env.decreaseIndent();
        sb.append(env.indent()).append("} catch (Exception e) {}\n");

        return sb.toString();
    }
}
