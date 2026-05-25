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

    public Expr keyExpr;

    public EncryptExpr(String keyName,
                       String formatName,
                       Expr payload) {

        this.keyName = keyName;
        this.formatName = formatName;
        this.payload = payload;
        this.keyExpr = new Expr.StringLiteral(keyName);
    }

    public EncryptExpr(Expr payload,
                       Expr keyExpr) {

        this.payload = payload;
        this.keyExpr = keyExpr;
        this.keyName = extractKeyName(keyExpr);
        this.formatName = extractFormatName(payload);
    }

    @Override
    public Value eval(Environment env) {
        Value payloadValue = payload.eval(env);
        Value keyValue = keyExpr.eval(env);

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
    public Operators typecheck(TypeEnv delta,
                               LabelEnv gamma) {

        Type keyType = Operators.runtimeType(keyExpr.typecheck(delta, gamma));
        if (keyType != Type.STRING) {
            throw new TypeCheckException("Encryption key must have type STRING");
        }

        Operators payloadType = payload.typecheck(delta, gamma);

        // Infer the ciphertext's format from the payload type, not from the payload syntax.
        // In particular: encrypting a variable `u` where `u : Transfer1` should yield
        // CiphertextType(key, "Transfer1"), not CiphertextType(key, "u").
        String inferredFormatName = this.formatName;
        if (payloadType instanceof FormatType ft) {
            inferredFormatName = ft.name;
        } else if (payloadType instanceof CiphertextType ct) {
            // Nested encryption: preserve the inner format name.
            inferredFormatName = ct.formatName;
        }

        this.formatName = inferredFormatName;

        return new CiphertextType(keyName, inferredFormatName);
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        return SecLabel.LOW;
    }

    @Override
    public String compile(CodeGenEnv env) {
        return "Crypto.encrypt(" + payload.compile(env) + ", " + keyExpr.compile(env) + ")";
    }

    private String extractKeyName(Expr keyExpr) {
        if (keyExpr instanceof Expr.StringLiteral literal) {
            return literal.value;
        }

        if (keyExpr instanceof VarExpr varExpr) {
            return varExpr.name;
        }

        throw new RuntimeException("Unsupported encryption key expression");
    }

    private String extractFormatName(Expr expr) {
        if (expr instanceof ConstructorExpr constructorExpr) {
            return constructorExpr.name;
        }

        if (expr instanceof VarExpr varExpr) {
            return varExpr.name;
        }

        if (expr instanceof EncryptExpr encryptExpr) {
            return encryptExpr.formatName;
        }

        if (expr instanceof Expr.StringLiteral stringLiteral) {
            return stringLiteral.value;
        }

        return expr.getClass().getSimpleName();
    }
}
