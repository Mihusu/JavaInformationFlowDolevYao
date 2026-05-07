package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.UUID;

public class EncryptExpr extends Expr {
    public Expr payload;
    public Expr key;

    public EncryptExpr(Expr payload, Expr key) {
        this.payload = payload;
        this.key = key;
    }

    @Override
    public Value eval(Environment env) {
        Value payloadValue = payload.eval(env);
        Value keyValue = key.eval(env);

        if (!(keyValue instanceof StringValue sk)) {
            throw new RuntimeException("Key must be string");
        }

        EncryptedValue encrypted = new EncryptedValue(
                sk.value,
                payloadValue,
                UUID.randomUUID().toString()
        );
        encrypted.label = SecLabel.LOW;
        return encrypted;
    }

    @Override
    public Type typecheck(TypeEnv delta, LabelEnv gamma) {
        Type keyType = key.typecheck(delta, gamma);

        if (keyType != Type.STRING) {
            throw new TypeCheckException("Encryption key must have type STRING");
        }

        payload.typecheck(delta, gamma);
        return Type.CIPHERTEXT;
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        return SecLabel.LOW;
    }

    @Override
    public String compile(CodeGenEnv env) {
        return "Crypto.encrypt(" + payload.compile(env) + ", " + key.compile(env) + ")";
    }
}
