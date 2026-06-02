package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Base64;

/**
 * Pattern node that decrypts an incoming ciphertext and matches its plaintext structure.
 */
public class EncryptFormat extends Format {
    public Expr key;
    public Format inner;

    public EncryptFormat(Expr key, Format inner) {
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

        try {
            byte[] keyBytes = Arrays.copyOf(sk.value.getBytes("UTF-8"), 16);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(enc.nonce));
            ByteArrayInputStream bis = new ByteArrayInputStream(decryptedBytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Value decryptedValue = (Value) ois.readObject();

            return inner.match(decryptedValue, env);
        } catch (Exception e) {
            // Decryption failed or some other issue
            return false;
        }
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel label) {

        // Verify the key expression is well-typed
        Type keyType = Operators.runtimeType(key.typecheck(delta, gamma));

        if (keyType != Type.STRING) {
            throw new TypeCheckException(
                    "Encryption key " + key + " must have type STRING"
            );
        }

        inner.typecheck(delta, gamma, label);
    }

    public CiphertextType ciphertextType() {
        return new CiphertextType(extractKeyName(key), extractFormatName(inner));
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

        String decrypted = env.freshVar("decrypted");
        sb.append(env.indent())
                .append("Object ").append(decrypted).append(" = Crypto.decrypt(").append(enc).append(", ")
                .append(key.compile(env)).append(");\n");

        sb.append(inner.compileMatch(env, decrypted));

        return sb.toString();
    }

    @Override
    public String describe() {
        String keyName = key instanceof Expr.StringLiteral literal
                ? literal.value
                : key.compile(new CodeGenEnv());
        return "e(" + keyName + ", " + inner.describe() + ")";
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        return inner.label(gamma);
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

    private String extractFormatName(Format format) {
        if (format instanceof ConstructorFormat constructorFormat) {
            return constructorFormat.name;
        }

        if (format instanceof TypedVarFormat typedVarFormat) {
            return typedVarFormat.name;
        }

        if (format instanceof EncryptFormat encryptFormat) {
            return encryptFormat.ciphertextType().formatName;
        }

        throw new RuntimeException("Unsupported encrypted format pattern");
    }
}
