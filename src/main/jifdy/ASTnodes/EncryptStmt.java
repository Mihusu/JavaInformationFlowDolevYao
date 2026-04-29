package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

import java.util.UUID;

public class EncryptStmt extends Stmt {

    public String target;
    public Expr payload;
    public Expr key;

    public EncryptStmt(String target, Expr payload, Expr key) {
        this.target = target;
        this.payload = payload;
        this.key = key;
    }

    @Override
    public void eval(Environment env) {

        Value payloadValue = payload.eval(env);
        Value keyValue = key.eval(env);

        if (!(keyValue instanceof StringValue sk)) {
            throw new RuntimeException("Key must be string");
        }

        // Set up a random value/nonce
        String nonce = UUID.randomUUID().toString();

        env.setVariables(target, new EncryptedValue(sk.value, payloadValue, nonce));
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        Type payloadType = payload.typecheck(delta, gamma);

        Type targetType = delta.getType(target);

        if (targetType != Type.CIPHERTEXT) {
            throw new TypeCheckException(
                    "Target of encryption must have type CIPHERTEXT"
            );
        }

        if (!targetType.equals(payloadType)) {
            throw new TypeCheckException(
                    "Ciphertext variable " + target +
                            " expects encrypted " + targetType +
                            " but got " + payloadType
            );
        }

        // encryption is the only operator allowed to lower the label
        SecLabel targetLabel = gamma.getLabel(target);

        if (!Security.canFlow(SecLabel.LOW, targetLabel)) {
            throw new TypeCheckException(
                    "Cannot store encrypted value into " + target
            );
        }
    }

    @Override
    public String compile(CodeGenEnv env) {

        return env.indent() +
                target + " = Crypto.encrypt(" +
                payload.compile(env) + ", " +
                key.compile(env) + ");\n";
    }
}
