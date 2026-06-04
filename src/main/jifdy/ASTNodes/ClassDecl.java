package ASTNodes;

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
    public String superName;

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
        env.putClass(this);

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

        sb.append(env.indent())
                .append("public class ")
                .append(name);

        if (superName != null) {
            sb.append(" extends ").append(superName);
        }

        sb.append(" {\n");

        env.increaseIndent();

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

        env.decreaseIndent();
        sb.append(env.indent()).append("}\n");

        return sb.toString();
    }

    /**
     * Performs type checking on the class fields and methods.
     * @param delta The type environment.
     * @param gamma The label environment.
     */
    public void typecheck(TypeEnv delta, LabelEnv gamma) {
        if (superName != null) {
            delta.getClassDecl(superName);
        }

        registerInheritedFields(delta, gamma);
        delta.putType("this", new ClassType(name));
        gamma.putLabel("this", SecLabel.LOW);

        // First: register variables
        for (Declaration d : declarations) {

            if (d instanceof VarDecl v) {

                // Add variable to environments
                delta.putType(v.name, v.type);
                gamma.putLabel(v.name, v.label);

                // If initialized, check the initializer
                if (v.init != null) {

                    Operators initType = v.init.typecheck(delta, gamma);

                    if (!delta.isSubtype(initType, v.type)) {
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

    public void initializeObject(Environment env, ObjectValue object) {
        if (superName != null) {
            env.getClassDecl(superName).initializeObject(env, object);
        }

        Environment objectEnv = new Environment(env);
        objectEnv.setThisObject(object);

        for (Declaration d : declarations) {
            if (d instanceof VarDecl v) {
                Value initVal = (v.init != null)
                        ? v.init.eval(objectEnv)
                        : defaultObjectFieldValue(env, v.type);

                initVal.label = v.label;
                object.fields.put(v.name, initVal);
                object.fieldLabels.put(v.name, v.label);
            }
        }
    }

    private Value defaultObjectFieldValue(Environment env, Operators type) {
        if (type instanceof ClassType classType) {
            return env.instantiate(classType.name());
        }

        return JavaTypeSupport.defaultValue(type);
    }

    public MethodDecl findMethod(String methodName, TypeEnv delta) {
        for (MethodDecl method : methods) {
            if (method.name.equals(methodName)) {
                return method;
            }
        }

        if (superName != null) {
            return delta.getClassDecl(superName).findMethod(methodName, delta);
        }

        return null;
    }

    public MethodDecl findMethod(String methodName, Environment env) {
        for (MethodDecl method : methods) {
            if (method.name.equals(methodName)) {
                return method;
            }
        }

        if (superName != null) {
            return env.getClassDecl(superName).findMethod(methodName, env);
        }

        return null;
    }

    public VarDecl findField(String fieldName, TypeEnv delta) {
        for (Declaration declaration : declarations) {
            if (declaration instanceof VarDecl field && field.name.equals(fieldName)) {
                return field;
            }
        }

        if (superName != null) {
            return delta.getClassDecl(superName).findField(fieldName, delta);
        }

        return null;
    }

    private void registerInheritedFields(TypeEnv delta, LabelEnv gamma) {
        if (superName == null) {
            return;
        }

        ClassDecl parent = delta.getClassDecl(superName);
        parent.registerInheritedFields(delta, gamma);
        for (Declaration declaration : parent.declarations) {
            if (declaration instanceof VarDecl field) {
                delta.putType(field.name, field.type);
                gamma.putLabel(field.name, field.label);
            }
        }
    }
}
