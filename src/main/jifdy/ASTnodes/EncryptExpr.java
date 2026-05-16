package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

/**
 * Expression node that encrypts a payload expression with a string key.
 */
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

        try {
            byte[] keyBytes = Arrays.copyOf(sk.value.getBytes("UTF-8"), 16);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(payloadValue);
            byte[] payloadBytes = bos.toByteArray();

            // We still use EncryptedValue but we could store the actual ciphertext if we wanted to be 100% realistic
            // For now, let's keep the EncryptedValue structure but emphasize that it's "encrypted"
            EncryptedValue encrypted = new EncryptedValue(
                    null,
                    null,
                    Base64.getEncoder().encodeToString(cipher.doFinal(payloadBytes))
            );
            encrypted.label = SecLabel.LOW;
            return encrypted;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
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
