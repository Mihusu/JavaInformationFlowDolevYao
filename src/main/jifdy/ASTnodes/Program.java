package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Program extends Node {
    public List<ClassDecl> classes;

    public Program(List<ClassDecl> classDecls) {
        this.classes = classDecls;
    }

    public void eval(Environment env) {
        for (ClassDecl c : classes) {
            c.eval(env);
        }
    }

    public void typecheck(TypeEnv delta, LabelEnv gamma) {
        for (ClassDecl c : classes) {
            c.typecheck(delta, gamma);
        }
    }

    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        sb.append("""
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
        """);

        for (ClassDecl c : classes) {
            sb.append(c.compile(env));
        }

        sb.append("""

            public static void main(String[] args) {
                GeneratedProgram program = new GeneratedProgram();
        """);

        for (ClassDecl c : classes) {
            appendMainCalls(sb, c);
        }

        sb.append("""
            }

        }
        """);

        return sb.toString();
    }

    private void appendMainCalls(StringBuilder sb, ClassDecl cls) {
        Set<String> fieldNames = new HashSet<>();
        for (Declaration declaration : cls.declarations) {
            if (declaration instanceof VarDecl varDecl) {
                fieldNames.add(varDecl.name);
            }
        }

        for (FunctionDecl function : cls.functions) {
            sb.append("        program.")
                    .append(function.name)
                    .append("(");

            for (int i = 0; i < function.params.size(); i++) {
                Param param = function.params.get(i);
                if (fieldNames.contains(param.name)) {
                    sb.append("program.").append(param.name);
                } else {
                    sb.append(defaultJavaValue(param.type));
                }

                if (i < function.params.size() - 1) {
                    sb.append(", ");
                }
            }

            sb.append(");\n");
        }
    }

    private String defaultJavaValue(Type type) {
        return switch (type) {
            case INT -> "0";
            case BOOL -> "false";
            case STRING -> "\"\"";
            case CIPHERTEXT -> "new EncryptedValue(\"\", \"\", \"\")";
        };
    }
}
