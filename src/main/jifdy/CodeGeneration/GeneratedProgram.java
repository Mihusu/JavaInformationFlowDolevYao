 import java.util.*;
 import javax.crypto.Cipher;
 import javax.crypto.spec.SecretKeySpec;
 import java.io.*;
 
 public class GeneratedProgram {
 
     static class EncryptedValue implements Serializable {
         byte[] ciphertext;
         byte[] salt; // This is just a mock for simplicity. 
 
         EncryptedValue(byte[] ciphertext) {
             this.ciphertext = ciphertext;
         }
     }
 
     static class ConstructorValue implements Serializable {
         String name;
         List<Object> values;
 
         ConstructorValue(String n, List<Object> v) {
             name = n;
             values = v;
         }
     }
 
     static class Crypto {
         private static final String ALGORITHM = "AES";

         static EncryptedValue encrypt(Object payload, String key) {
             try {
                 byte[] keyBytes = Arrays.copyOf(key.getBytes("UTF-8"), 16);
                 SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
                 Cipher cipher = Cipher.getInstance(ALGORITHM);
                 cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                 
                 ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos);
                 oos.writeObject(payload);
                 byte[] payloadBytes = bos.toByteArray();
                 
                 return new EncryptedValue(cipher.doFinal(payloadBytes));
             } catch (Exception e) {
                 throw new RuntimeException("Encryption failed", e);
             }
         }

         static Object decrypt(EncryptedValue enc, String key) {
             try {
                 byte[] keyBytes = Arrays.copyOf(key.getBytes("UTF-8"), 16);
                 SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
                 Cipher cipher = Cipher.getInstance(ALGORITHM);
                 cipher.init(Cipher.DECRYPT_MODE, secretKey);
                 
                 byte[] decryptedBytes = cipher.doFinal(enc.ciphertext);
                 
                 ByteArrayInputStream bis = new ByteArrayInputStream(decryptedBytes);
                 ObjectInputStream ois = new ObjectInputStream(bis);
                 return ois.readObject();
             } catch (Exception e) {
                 throw new RuntimeException("Decryption failed", e);
             }
         }
     }
     static class Channel {
        private final Queue<Object> messages = new ArrayDeque<>();

        void send(Object message) {
            messages.add(message);
        }

        Object receive() {
            if (messages.isEmpty()) {
                throw new RuntimeException("No message available");
            }

            return messages.remove();
        }
    }

    final Channel channel = new Channel();
public String user = "Alice";
public String target = "Bob";

public void client() {
    int amount1 = 2000;
    int amount2 = 3000;
    int total = 0;
    EncryptedValue msg1 = Crypto.encrypt(new ConstructorValue("Transfer1", Arrays.asList(user, amount1, target)), "kClient");
    EncryptedValue msg2 = Crypto.encrypt(new ConstructorValue("Transfer2", Arrays.asList(user, amount2, target)), "kClient");
    total = amount1 + amount2;
    channel.send(msg1);
    channel.send(msg2);
    System.out.println("Transferred " + total + " to " + target);
}

public void bank() {
    int total = 0;
    boolean done = false;
    int amount1 = 0;
    int amount2 = 0;
    try {
        Object msg_0 = channel.receive();
        if (!(msg_0 instanceof EncryptedValue)) throw new RuntimeException();
        EncryptedValue enc_1 = (EncryptedValue)msg_0;
        Object decrypted_2 = Crypto.decrypt(enc_1, "kClient");
        if (!(decrypted_2 instanceof ConstructorValue)) throw new RuntimeException();
        ConstructorValue cv_3 = (ConstructorValue)decrypted_2;
        if (!cv_3.name.equals("Transfer1")) throw new RuntimeException();
        user = (String) cv_3.values.get(0);
        amount1 = (Integer) cv_3.values.get(1);
        target = (String) cv_3.values.get(2);
    } catch (Exception e) {}
    try {
        Object msg_4 = channel.receive();
        if (!(msg_4 instanceof EncryptedValue)) throw new RuntimeException();
        EncryptedValue enc_5 = (EncryptedValue)msg_4;
        Object decrypted_6 = Crypto.decrypt(enc_5, "kClient");
        if (!(decrypted_6 instanceof ConstructorValue)) throw new RuntimeException();
        ConstructorValue cv_7 = (ConstructorValue)decrypted_6;
        if (!cv_7.name.equals("Transfer2")) throw new RuntimeException();
        user = (String) cv_7.values.get(0);
        amount2 = (Integer) cv_7.values.get(1);
        target = (String) cv_7.values.get(2);
    } catch (Exception e) {}
    total = amount1 + amount2;
    done = true;
    EncryptedValue resultMsg = Crypto.encrypt(new ConstructorValue("Result", Arrays.asList(user, done, total)), "kReceiver");
    channel.send(resultMsg);
}

public void clientReceiver() {
    try {
        Object msg_8 = channel.receive();
        if (!(msg_8 instanceof EncryptedValue)) throw new RuntimeException();
        EncryptedValue enc_9 = (EncryptedValue)msg_8;
        Object decrypted_10 = Crypto.decrypt(enc_9, "kReceiver");
        if (!(decrypted_10 instanceof ConstructorValue)) throw new RuntimeException();
        ConstructorValue cv_11 = (ConstructorValue)decrypted_10;
        if (!cv_11.name.equals("Result")) throw new RuntimeException();
        user = (String) cv_11.values.get(0);
        boolean done = (Boolean) cv_11.values.get(1);
        int total = (Integer) cv_11.values.get(2);
        System.out.println("You received money from " + user + ". " + user + " has sent you " + total + ". Status: " + done);
    } catch (Exception e) {}
}

    public static void main(String[] args) {
        GeneratedProgram program = new GeneratedProgram();
program.client();
program.bank();
program.clientReceiver();
    }

}
