package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Utils.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

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
    public void labelTypeCheck(TypeEnv delta, LabelEnv gamma) {

        // Verify the key expression is well-typed
        Type keyType = Types.type(key.labelTypeCheck(delta, gamma));

        if (keyType != Type.STRING) {
            throw new TypeCheckException(
                    "Encryption key " + key + " must have type STRING"
            );
        }

        inner.labelTypeCheck(delta, gamma);
    }

    public CiphertextType ciphertextType() {
        return new CiphertextType(
                EncryptionTypeSupport.extractKeyName(key),
                EncryptionTypeSupport.extractFormatName(inner)
        );
    }

    @Override
    public String compile(CodeGenEnv env, String valueVar) {

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

        sb.append(inner.compile(env, decrypted));

        return sb.toString();
    }

    /**
     * Delegates binding collection through the encryption layer.
     *
     * @param bindings output map from variable name to source type.
     */
    @Override
    public void collectBindings(Map<String, Types> bindings) {
        inner.collectBindings(bindings);
    }

    /**
     * Delegates binding-label collection through the encryption layer.
     *
     * @param labels output map from variable name to security label.
     */
    @Override
    public void collectBindingLabels(Map<String, SecLabel> labels) {
        inner.collectBindingLabels(labels);
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

}
