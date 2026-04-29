package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

public class EncryptPattern extends ReceivePattern {
    public Expr key;
    public ReceivePattern inner;

    public EncryptPattern(Expr key, ReceivePattern inner) {
        this.key = key;
        this.inner = inner;
    }

    @Override
    public boolean match(Value value, Environment env) {

        if (!(value instanceof EncryptedValue enc)) {
            return false;
        }

        Value keyValue = key.eval(env);

        if (!(keyValue instanceof StringValue sk)) {
            return false;
        }

        if (!enc.key.equals(sk.value)) {
            return false;
        }

        return inner.match(enc.payload, env);
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        // Verify the key expression is well-typed
        Type keyType = delta.getType(key.toString());

        if (keyType != Type.STRING) {
            throw new TypeCheckException(
                    "Encryption key " + key + " must have type STRING"
            );
        }

        inner.typecheck(delta, gamma, SecLabel.HIGH);
    }

    @Override
    public String compile(CodeGenEnv env) {
        return "";
    }

    @Override
    public String compileMatch(CodeGenEnv env, String valueVar) {

        String enc = env.freshVar("enc");

        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("if (!(").append(valueVar)
                .append(" instanceof EncryptedValue)) throw new RuntimeException();\n");

        sb.append(env.indent())
                .append("EncryptedValue ").append(enc)
                .append(" = (EncryptedValue)").append(valueVar).append(";\n");

        sb.append(env.indent())
                .append("if (!").append(enc).append(".key.equals(")
                .append(key.compile(env)).append(")) throw new RuntimeException();\n");

        sb.append(inner.compileMatch(env, enc + ".payload"));

        return sb.toString();
    }
}
