package Demos;

import java.util.*;


public class Demo {

    // =========================
    // Runtime Values
    // =========================

    static abstract class Value {}

    static class IntValue extends Value {
        int value;
        IntValue(int v) { value = v; }
    }

    static class BoolValue extends Value {
        boolean value;  
        BoolValue(boolean v) { value = v; }
    }

    static class StringValue extends Value {
        String value;
        StringValue(String v) { value = v; }
    }

    static class ConstructorValue extends Value {
        String name;
        List<Object> args;

        ConstructorValue(String name, List<Object> args) {
            this.name = name;
            this.args = args;
        }
    }

    static class EncryptedValue extends Value {
        String key;
        Object payload;
        String nonce;

        EncryptedValue(String key, Object payload, String nonce) {
            this.key = key;
            this.payload = payload;
            this.nonce = nonce;
        }
    }

    // =========================
    // Crypto Helper
    // =========================

    static class Crypto {
        static EncryptedValue encrypt(Object payload, String key) {
            return new EncryptedValue(
                    key,
                    payload,
                    UUID.randomUUID().toString()
            );
        }
    }

    // =========================
    // Channel (send / receive)
    // =========================

    static class Channel {

        Queue<Object> queue = new LinkedList<>();

        void send(Object msg) {
            queue.add(msg);
        }

        Object receive() {
            if (queue.isEmpty()) {
                throw new RuntimeException("No message available");
            }
            return queue.poll();
        }
    }

    static Channel channel = new Channel();

    // =========================
    // USER PROGRAM STARTS HERE
    // =========================

    public static void main(String[] args) {

        // Example generated code (replace with your compile output)

        int secret = 42;
        String key = "k";

        EncryptedValue cipher;

        cipher = Crypto.encrypt(secret, key);

        channel.send(cipher);

        try {
            Object msg_0 = channel.receive();

            if (!(msg_0 instanceof EncryptedValue)) throw new RuntimeException();
            EncryptedValue enc2 = (EncryptedValue) msg_0;

            if (!enc2.key.equals(key)) throw new RuntimeException();

            if (!(enc2.payload instanceof ConstructorValue)) throw new RuntimeException();
            ConstructorValue cv2 = (ConstructorValue) enc2.payload;

            Object X = cv2.args.get(0);
            Object Y = cv2.args.get(1);

            System.out.println(X);

        } catch (Exception e) {
            // receive failed -> ignore
        }
    }
}
