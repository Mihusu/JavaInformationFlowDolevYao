package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the entire program, consisting of a list of class declarations.
 */
public class Program extends Node {
    /**
     * The list of classes in the program.
     */
    public List<ClassDecl> classes;
    public Set<String> declaredKeys;
    public Set<String> declaredFormats;
    public Map<String, FormatType> formatTypes;

    /**
     * Constructs a Program with the given class declarations.
     * @param classDecls The list of class declarations.
     */
    public Program(List<ClassDecl> classDecls) {
        this(classDecls, Set.of(), Set.of(), Map.of());
    }

    public Program(List<ClassDecl> classDecls, Set<String> declaredKeys, Set<String> declaredFormats, Map<String, FormatType> formatTypes) {
        this.classes = classDecls;
        this.declaredKeys = declaredKeys;
        this.declaredFormats = declaredFormats;
        this.formatTypes = formatTypes;
    }

    /**
     * Evaluates (interprets) the program.
     * @param env The execution environment.
     */
    public void eval(Environment env) {
        for (ClassDecl c : classes) {
            c.eval(env);
        }
    }

    /**
     * Performs type checking on the entire program.
     * @param delta The type environment.
     * @param gamma The label environment.
     */
    public void typecheck(TypeEnv delta, LabelEnv gamma) {
        for (FormatType formatType : formatTypes.values()) {
            delta.putFormat(formatType.name, formatType);
        }

        for (ClassDecl c : classes) {
            c.typecheck(delta, gamma);
        }
    }

    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        sb.append("""
       package CodeGeneration;         
        
       import java.util.*;
       import javax.crypto.Cipher;
       import javax.crypto.spec.SecretKeySpec;
       import java.io.*;
       \s
       public class GeneratedProgram {
       \s
           static class EncryptedValue implements Serializable {
               byte[] ciphertext;
               String description;
               byte[] salt; // This is just a mock for simplicity.\s
       \s
               EncryptedValue(byte[] ciphertext, String description) {
                   this.ciphertext = ciphertext;
                   this.description = description;
               }
           }
       \s
           static class ConstructorValue implements Serializable {
               String name;
               List<Object> values;
       \s
               ConstructorValue(String n, List<Object> v) {
                   name = n;
                   values = v;
               }
           }
       \s
           static class Crypto {
               private static final String ALGORITHM = "AES";

               static EncryptedValue encrypt(Object payload, String key) {
                   try {
                       byte[] keyBytes = Arrays.copyOf(key.getBytes("UTF-8"), 16);
                       SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
                       Cipher cipher = Cipher.getInstance(ALGORITHM);
                       cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                       \s
                       ByteArrayOutputStream bos = new ByteArrayOutputStream();
                       ObjectOutputStream oos = new ObjectOutputStream(bos);
                       oos.writeObject(payload);
                       byte[] payloadBytes = bos.toByteArray();
                       \s
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
                       \s
                       byte[] decryptedBytes = cipher.doFinal(enc.ciphertext);
                       \s
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
       \s""");

        sb.append("""
          \s
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

           final Channel channel = new Channel();
       \s""");

        for (ClassDecl c : classes) {
            sb.append(c.compile(env));
        }

        sb.append("""

            public static void main(String[] args) {
                GeneratedProgram program = new GeneratedProgram();
        """);

        for (ClassDecl c : classes) {
            appendMainEntry(sb, c);
        }

        sb.append("""
            }

        }
        """);

        return sb.toString();
    }

    private void appendMainEntry(StringBuilder sb, ClassDecl cls) {
        if (!cls.statements.isEmpty()) {
            sb.append("program.entry();\n");
            return;
        }

        Set<String> fieldNames = new HashSet<>();
        for (Declaration declaration : cls.declarations) {
            if (declaration instanceof VarDecl varDecl) {
                fieldNames.add(varDecl.name);
            }
        }

        for (MethodDecl method : cls.methods) {
            sb.append("program.")
                    .append(method.name)
                    .append("(");

            for (int i = 0; i < method.params.size(); i++) {
                Param param = method.params.get(i);
                if (fieldNames.contains(param.name)) {
                    sb.append("program.").append(param.name);
                } else {
                    sb.append(JavaTypeSupport.defaultValueExpression(param.type));
                }

                if (i < method.params.size() - 1) {
                    sb.append(", ");
                }
            }

            sb.append(");\n");
        }
    }
}
