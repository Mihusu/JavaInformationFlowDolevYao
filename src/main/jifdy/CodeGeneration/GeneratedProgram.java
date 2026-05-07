package CodeGeneration;

import java.util.*;

public class GeneratedProgram {

    static class EncryptedValue {
        String key;
        Object payload;
        String nonce;

        EncryptedValue(String k, Object p, String n) {
            key = k;
            payload = p;
            nonce = n;
        }
    }

    static class ConstructorValue {
        String name;
        List<Object> values;

        ConstructorValue(String n, List<Object> v) {
            name = n;
            values = v;
        }
    }

    static class Crypto {
        static EncryptedValue encrypt(Object payload, String key) {
            return new EncryptedValue(
                key,
                payload,
                UUID.randomUUID().toString()
            );
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
        EncryptedValue msg1 = Crypto.encrypt(new ConstructorValue("Transfer", Arrays.asList(user, amount1, target)), "kBank");
        EncryptedValue msg2 = Crypto.encrypt(new ConstructorValue("Transfer", Arrays.asList(user, amount2, target)), "kBank");
        channel.send(msg1);
        channel.send(msg2);
        try {
            Object msg_0 = channel.receive();
            if (!(msg_0 instanceof EncryptedValue)) throw new RuntimeException();
            EncryptedValue enc_1 = (EncryptedValue)msg_0;
            if (!enc_1.key.equals("kClient")) throw new RuntimeException();
            if (!(enc_1.payload instanceof ConstructorValue)) throw new RuntimeException();
            ConstructorValue cv_2 = (ConstructorValue)enc_1.payload;
            if (!cv_2.name.equals("Result")) throw new RuntimeException();
            boolean done = (Boolean) cv_2.values.get(0);
            int total = (Integer) cv_2.values.get(1);
            target = (String) cv_2.values.get(2);
            System.out.println("The transfer was sent successfully to " + target + " with the total amount " + total + "Status: " + done);
        } catch (Exception e) {}
    }

    public void bank() {
        int total = 0;
        boolean done = false;
        try {
            Object msg_3 = channel.receive();
            if (!(msg_3 instanceof EncryptedValue)) throw new RuntimeException();
            EncryptedValue enc_4 = (EncryptedValue)msg_3;
            if (!enc_4.key.equals("kBank")) throw new RuntimeException();
            if (!(enc_4.payload instanceof ConstructorValue)) throw new RuntimeException();
            ConstructorValue cv_5 = (ConstructorValue)enc_4.payload;
            if (!cv_5.name.equals("Transfer")) throw new RuntimeException();
            user = (String) cv_5.values.get(0);
            int amount = (Integer) cv_5.values.get(1);
            target = (String) cv_5.values.get(2);
            total = total + amount;
        } catch (Exception e) {}
        try {
            Object msg_6 = channel.receive();
            if (!(msg_6 instanceof EncryptedValue)) throw new RuntimeException();
            EncryptedValue enc_7 = (EncryptedValue)msg_6;
            if (!enc_7.key.equals("kBank")) throw new RuntimeException();
            if (!(enc_7.payload instanceof ConstructorValue)) throw new RuntimeException();
            ConstructorValue cv_8 = (ConstructorValue)enc_7.payload;
            if (!cv_8.name.equals("Transfer")) throw new RuntimeException();
            user = (String) cv_8.values.get(0);
            int amount2 = (Integer) cv_8.values.get(1);
            target = (String) cv_8.values.get(2);
            total = total + amount2;
            done = true;
        } catch (Exception e) {}
        EncryptedValue resultMsg = Crypto.encrypt(new ConstructorValue("Result", Arrays.asList(done, total, target)), "kClient");
        channel.send(resultMsg);
    }

    public static void main(String[] args) {
        GeneratedProgram program = new GeneratedProgram();
        program.client();
        program.bank();
    }

}
