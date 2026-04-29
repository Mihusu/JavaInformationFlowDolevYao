package ASTnodes;

public class EncryptedValue extends Value {

    public String key;
    public Value payload;
    public String nonce;

    public EncryptedValue(String key, Value payload, String nonce) {
        this.key = key;
        this.payload = payload;
        this.nonce = nonce;
    }

    @Override
    public Type getType() {
        return Type.CIPHERTEXT;
    }
}
