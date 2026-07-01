package CodeGeneration;         
 
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
 
public class GeneratedProgram {
    private static final Scanner INPUT = new Scanner(System.in);
 
    static class EncryptedValue implements Serializable {
        byte[] ciphertext;
        String description;
        byte[] salt; // This is just a mock for simplicity. 
 
        EncryptedValue(byte[] ciphertext, String description) {
            this.ciphertext = ciphertext;
            this.description = description;
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
                 
                return new EncryptedValue(
                        cipher.doFinal(payloadBytes),
                        "e(" + key + ", " + describe(payload) + ")"
                );
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

        static String describe(Object value) {
            if (value instanceof EncryptedValue encryptedValue) {
                return encryptedValue.description;
            }
            if (value instanceof ConstructorValue constructorValue) {
                return constructorValue.name + "(...)";
            }
            return String.valueOf(value);
        }
    }
     
    static class Channel {
        private final Queue<Object> messages = new ArrayDeque<>();

        void send(Object message) {
            System.out.println("[JIFDY] SEND -> network: " + Crypto.describe(message));
            messages.add(message);
        }

        Object peek() {
            if (messages.isEmpty()) {
                throw new RuntimeException("No message available");
            }

            return messages.element();
        }

        void remove() {
            messages.remove();
        }
    }

    static final Channel channel = new Channel();
 public static class BankTransfer {
    public String user = "Alice";
    public String target = "Bob";
    public BankTransfer() {
        user = this.user;
        target = this.target;
    }

    public void client() {
        int amount1 = 0;
        int amount2 = 0;
        int amount3 = 0;
        int amount4 = 0;
        int total = 0;
        System.out.println("Welcome to the bank.");
        System.out.println("Please enter the amounts you want to transfer.");
        System.out.println("In this program you can transfer money up to 3 times. You can deduct money after the third transfer.");
        System.out.println("Enter amount1:");
        System.out.print("[INPUT] amount1 = ");
        String input_0 = INPUT.nextLine();
        amount1 = Integer.parseInt(input_0.trim());
        System.out.println("Enter amount2:");
        System.out.print("[INPUT] amount2 = ");
        String input_1 = INPUT.nextLine();
        amount2 = Integer.parseInt(input_1.trim());
        System.out.println("Enter amount3:");
        System.out.print("[INPUT] amount3 = ");
        String input_2 = INPUT.nextLine();
        amount3 = Integer.parseInt(input_2.trim());
        System.out.println("Enter how much you want to deduct: ");
        System.out.print("[INPUT] amount4 = ");
        String input_3 = INPUT.nextLine();
        amount4 = Integer.parseInt(input_3.trim());
        ConstructorValue u = new ConstructorValue("Transfer1", Arrays.asList(user, amount1, target));
        EncryptedValue msg1 = Crypto.encrypt(u, "kClientBank");
        EncryptedValue msg2 = Crypto.encrypt(new ConstructorValue("Transfer2", Arrays.asList(user, amount2, target)), "kClientBank");
        EncryptedValue msg3 = Crypto.encrypt(new ConstructorValue("Transfer3", Arrays.asList(user, amount3 - amount4, target)), "kClientBank");
        total = amount1 + amount2 + amount3 - amount4;
        if (total < 0) {
            System.out.println("Transfer failed.");
        }
        else {
            channel.send(msg1);
            channel.send(msg2);
            channel.send(msg3);
            System.out.println("Transferred " + total + " to " + target);
        }
    }

    public void bank() {
        int total = 0;
        boolean done = false;
        int amount1 = 0;
        int amount2 = 0;
        int amount3 = 0;
        String user = "";
        String target = "";
        System.out.println("[JIFDY] network -> TRY_RCV: e(kClientBank, Transfer1(user, amount1, target))");
        try {
            Object msg_4 = channel.peek();
            if (!(msg_4 instanceof EncryptedValue)) throw new RuntimeException();
            EncryptedValue enc_5 = (EncryptedValue)msg_4;
            Object decrypted_6 = Crypto.decrypt(enc_5, "kClientBank");
            if (!(decrypted_6 instanceof ConstructorValue)) throw new RuntimeException();
            ConstructorValue cv_7 = (ConstructorValue)decrypted_6;
            if (!cv_7.name.equals("Transfer1")) throw new RuntimeException();
            user = (String) cv_7.values.get(0);
            amount1 = (int) cv_7.values.get(1);
            target = (String) cv_7.values.get(2);
            channel.remove();
        } catch (Exception e) {
        }
        System.out.println("[JIFDY] network -> TRY_RCV: e(kClientBank, Transfer2(user, amount2, target))");
        try {
            Object msg_8 = channel.peek();
            if (!(msg_8 instanceof EncryptedValue)) throw new RuntimeException();
            EncryptedValue enc_9 = (EncryptedValue)msg_8;
            Object decrypted_10 = Crypto.decrypt(enc_9, "kClientBank");
            if (!(decrypted_10 instanceof ConstructorValue)) throw new RuntimeException();
            ConstructorValue cv_11 = (ConstructorValue)decrypted_10;
            if (!cv_11.name.equals("Transfer2")) throw new RuntimeException();
            user = (String) cv_11.values.get(0);
            amount2 = (int) cv_11.values.get(1);
            target = (String) cv_11.values.get(2);
            channel.remove();
        } catch (Exception e) {
        }
        System.out.println("[JIFDY] network -> TRY_RCV: e(kClientBank, Transfer3(user, amount3, target))");
        try {
            Object msg_12 = channel.peek();
            if (!(msg_12 instanceof EncryptedValue)) throw new RuntimeException();
            EncryptedValue enc_13 = (EncryptedValue)msg_12;
            Object decrypted_14 = Crypto.decrypt(enc_13, "kClientBank");
            if (!(decrypted_14 instanceof ConstructorValue)) throw new RuntimeException();
            ConstructorValue cv_15 = (ConstructorValue)decrypted_14;
            if (!cv_15.name.equals("Transfer3")) throw new RuntimeException();
            user = (String) cv_15.values.get(0);
            amount3 = (int) cv_15.values.get(1);
            target = (String) cv_15.values.get(2);
            channel.remove();
        } catch (Exception e) {
        }
        total = amount1 + amount2 + amount3;
        if (total < 0) {
            System.out.println("Transfer failed.");
        }
        else {
            done = true;
            EncryptedValue resultMsg = Crypto.encrypt(new ConstructorValue("Result", Arrays.asList(user, done, total)), "kReceiverBank");
            channel.send(resultMsg);
        }
    }

    public void clientReceiver() {
        String user = "";
        boolean done = false;
        int total = 0;
        System.out.println("[JIFDY] network -> TRY_RCV: e(kReceiverBank, Result(user, done, total))");
        try {
            Object msg_16 = channel.peek();
            if (!(msg_16 instanceof EncryptedValue)) throw new RuntimeException();
            EncryptedValue enc_17 = (EncryptedValue)msg_16;
            Object decrypted_18 = Crypto.decrypt(enc_17, "kReceiverBank");
            if (!(decrypted_18 instanceof ConstructorValue)) throw new RuntimeException();
            ConstructorValue cv_19 = (ConstructorValue)decrypted_18;
            if (!cv_19.name.equals("Result")) throw new RuntimeException();
            user = (String) cv_19.values.get(0);
            done = (boolean) cv_19.values.get(1);
            total = (int) cv_19.values.get(2);
            channel.remove();
        } catch (Exception e) {
        }
        if (done && total > 0) {
            System.out.println("From bank:");
            System.out.println("You received money from " + user + ". " + user + " has sent you " + total + ". Status: " + done);
        }
    }

    public void start() {
        client();
        bank();
        clientReceiver();
    }
}

    public static void main(String[] args) {
        BankTransfer bankTransfer = new BankTransfer();
        bankTransfer.start();
    }

}
