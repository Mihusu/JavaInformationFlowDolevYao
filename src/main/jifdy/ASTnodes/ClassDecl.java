package ASTnodes;

import ASTBuilder.Privacy;
import Analysis.*;
import CodeGeneration.CodeGenEnv;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a class declaration in the AST.
 */
public class ClassDecl extends Node {
    /**
     * Optional privacy setting for the class.
     */
    public Privacy privacy; // optional

    /**
     * The name of the class.
     */
    public String name;

    /**
     * List of field declarations in the class.
     */
    public List<Declaration> declarations;

    /**
     * List of method (method) declarations in the class.
     */
    public List<MethodDecl> methods;

    /**
     * Statements executed at class scope after declarations and method registration.
     */
    public List<Stmt> statements;

    /**
     * Evaluates the class by initializing its fields in the environment.
     * @param env The execution environment.
     */
    public void eval(Environment env) {

        // 1. Initialize declarations
        for (Declaration d : declarations) {
            if (d instanceof VarDecl v) {

                Value initVal = (v.init != null)
                        ? v.init.eval(env)
                        : JavaTypeSupport.defaultValue(v.type);

                initVal.label = v.label;

                env.labels.put(v.name, v.label);
                env.setVariables(v.name, initVal);
            } else {
                d.eval(env);
            }
        }

        // 2. Initialize methods
        for (MethodDecl f : methods) {
            f.eval(env);
        }

        // 3. Initialize statements
        for (Stmt stmt : statements) {
            stmt.eval(env);
        }
    }

    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        for (Declaration d : declarations) {
            if (d instanceof VarDecl v) {
                sb.append(v.compileField(env));
            } else {
                sb.append(d.compile(env));
            }
        }

        for (MethodDecl f : methods) {
            sb.append("\n").append(f.compile(env));
        }

        if (!statements.isEmpty()) {
            sb.append("\n")
                    .append(env.indent())
                    .append("public void entry() {\n");

            env.increaseIndent();
            for (Stmt stmt : statements) {
                sb.append(stmt.compile(env));
            }
            env.decreaseIndent();

            sb.append(env.indent()).append("}\n");
        }

        return sb.toString();
    }

    /**
     * Performs type checking on the class fields and methods.
     * @param delta The type environment.
     * @param gamma The label environment.
     */
    public void typecheck(TypeEnv delta, LabelEnv gamma) {

        // First: register variables
        for (Declaration d : declarations) {

            if (d instanceof VarDecl v) {

                // Add variable to environments
                delta.putType(v.name, v.type);
                gamma.putLabel(v.name, v.label);

                // If initialized, check the initializer
                if (v.init != null) {

                    Operators initType = v.init.typecheck(delta, gamma);

                    if (!Operators.sameType(initType, v.type)) {
                        throw new TypeCheckException(
                                "Type mismatch in declaration of " + v.name,
                                v.lineNumber,
                                v.name
                        );
                    }

                    SecLabel initLabel = v.init.label(gamma);

                    // SPECIAL CASE: Encryption (EncryptExpr) is a declassification mechanism.
                    // If the variable is initialized with an encryption expression and the label is LOW, allow the initialization.
                    if (!Security.canFlow(initLabel, v.label)) {
                        throw new TypeCheckException(
                                "Illegal information flow in initialization of "
                                        + v.name + ": "
                                        + initLabel + " -> " + v.label,
                                v.lineNumber,
                                v.name
                        );
                    }
                }
            }
        }

        // Then: register method signatures first
        for (MethodDecl f : methods) {

            List<Operators> paramTypes = new ArrayList<>();
            List<SecLabel> paramLabels = new ArrayList<>();

            for (Param p : f.params) {
                paramTypes.add(p.type);
                paramLabels.add(p.label);
            }

            delta.putMethod(
                    f.name,
                    new MethodType(paramTypes, f.returnType, paramLabels, f.returnLabel)
            );

            gamma.putMethod(
                    f.name,
                    new MethodLabel(paramLabels, f.returnLabel)
            );
        }

        // 1. Register class fields
        for (Declaration d : declarations) {
            d.typecheck(delta, gamma, SecLabel.LOW);
        }

        // 2. Register/check methods
        for (MethodDecl f : methods) {
            f.typecheck(delta, gamma, SecLabel.LOW);
        }

        for (Stmt stmt : statements) {
            stmt.typecheck(delta, gamma, SecLabel.LOW);
        }
    }

}
