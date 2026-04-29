package ASTnodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

import java.util.List;

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
        
            static class Crypto {
                static EncryptedValue encrypt(Object payload, String key) {
                    return new EncryptedValue(
                        key,
                        payload,
                        UUID.randomUUID().toString()
                    );
                }
            }
        
            public static void main(String[] args) {
        """);

        env.increaseIndent();

        for (ClassDecl c : classes) {
            sb.append(c.compile(env));
        }

        env.decreaseIndent();

        sb.append("""
            }
        }
        """);

        return sb.toString();
    }
}
