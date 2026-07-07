package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Utils.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * Expression node that encrypts a payload expression with a string key.
 */
public class EncryptExpr extends Expr {
    // Example:
    // kBank
    public String keyName;

    // Example:
    // Transfer
    public String formatName;

    // Actual payload term
    public Expr payload;

    // Actual expression used at runtime/code generation to get the key value
    public Expr keyExpr;

    public EncryptExpr(Expr payload,
                       Expr keyExpr) {

        this.payload = payload;
        this.keyExpr = keyExpr;
        this.keyName = EncryptionTypeSupport.extractKeyName(keyExpr);
        this.formatName = EncryptionTypeSupport.extractFormatName(payload);
    }

    @Override
    public Value eval(Environment env) {
        Value payloadValue = payload.eval(env);
        Value keyValue = keyExpr.eval(env);

        if (!(keyValue instanceof StringValue sk)) {
            throw new RuntimeException("Key must be string");
        }

        try {
            byte[] keyBytes = Arrays.copyOf(sk.value.getBytes(StandardCharsets.UTF_8), 16);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(payloadValue);
            byte[] payloadBytes = bos.toByteArray();

            // We still use EncryptedValue, but we could store the actual ciphertext if we wanted to be 100% realistic
            // For now, let's keep the EncryptedValue structure but emphasize that it's "encrypted"
            EncryptedValue encrypted = new EncryptedValue(
                    sk.value,
                    payloadValue,
                    Base64.getEncoder().encodeToString(cipher.doFinal(payloadBytes))
            );

            encrypted.label = SecLabel.LOW;
            return encrypted;

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public Types typeChecker(TypeEnv delta, LabelEnv gamma) {

        Type keyType = Types.type(keyExpr.typeChecker(delta, gamma));

        if (keyType != Type.STRING) {
            throw new TypeCheckException("Encryption key must have type STRING");
        }

        Types payloadType = payload.typeChecker(delta, gamma);

        // Infer the ciphertext's format from the payload type, not from the payload syntax.
        // In particular: encrypting a variable `u` where `u: Transfer1` should yield
        // CiphertextType(key, "Transfer1"), not CiphertextType(key, "u").
        String inferredFormatName = this.formatName;
        if (payloadType instanceof FormatType ft) {
            inferredFormatName = ft.name;
        } else if (payloadType instanceof CiphertextType ct) {
            // Nested encryption: preserve the inner format name.
            inferredFormatName = ct.formatName;
        }

        this.formatName = inferredFormatName;

        return new Types(keyName, inferredFormatName);
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        return SecLabel.LOW;
    }

    @Override
    public String compile(CodeGenEnv env) {
        return "Crypto.encrypt(" + payload.compile(env) + ", " + keyExpr.compile(env) + ")";
    }

}
