package ASTNodes;

import ASTBuilder.PublicPrivateLabel;
import Analysis.*;
import CodeGeneration.CodeGenEnv;
import Utils.TypeCheckException;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a class declaration in the AST.
 */
public class ClassDecl extends Node {

    public PublicPrivateLabel publicPrivateLabel; // optional
    public String name;
    public String superName;

    public List<Declaration> declarations;
    public List<MethodDecl> methods;
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

                Value initVal = (v.initExpression != null)
                        ? v.initExpression.eval(env)
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

    /**
     * Performs label and type checking on the class fields and methods.
     * @param delta The type environment.
     * @param gamma The label environment.
     */
    public void labelTypechecker(TypeEnv delta, LabelEnv gamma) {
        if (superName != null) {
            ClassDecl parent = delta.getClassDecl(superName);
            ConstructorDecl parentConstructor = parent.getConstructor();
            if (parentConstructor != null && !parentConstructor.params.isEmpty()) {
                throw new TypeCheckException(
                        "Parameterized superclass constructors are not supported for " + name
                );
            }
        }

        registerInheritedFields(delta, gamma);
        delta.putType("this", new ClassType(name));
        gamma.putLabel("this", SecLabel.LOW);
        validateConstructors();

        // First: register variables so field initializers and methods can refer to them.
        for (Declaration d : declarations) {

            if (d instanceof VarDecl v) {
                delta.putType(v.name, v.type);
                gamma.putLabel(v.name, v.label);
            }
        }

        // Then: register method signatures first
        for (MethodDecl f : methods) {

            List<Types> paramTypes = new ArrayList<>();
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
            d.labelTypeChecker(delta, gamma, SecLabel.LOW);
        }

        // 2. Register/check methods
        for (MethodDecl f : methods) {
            f.labelTypeChecker(delta, gamma, SecLabel.LOW);
        }

        for (Stmt stmt : statements) {
            stmt.labelTypeChecker(delta, gamma, SecLabel.LOW);
        }
    }

    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("public static class ")
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
                    .append("public void start() {\n");

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
     * Initializes a freshly allocated object by preparing inherited fields,
     * local fields, and finally running the matching constructor body.
     */
    public void initializeInstance(Environment env, ObjectValue object, List<Value> args) {
        if (superName != null) {
            ClassDecl parent = env.getClassDecl(superName);
            parent.initializeInstance(env, object, List.of());
        }

        initializeDeclaredFields(env, object);

        ConstructorDecl constructor = getConstructor();
        if (constructor == null) {
            if (!args.isEmpty()) {
                throw new RuntimeException(
                        "Class " + name + " has no declared constructor"
                );
            }
            return;
        }

        if (constructor.params.size() != args.size()) {
            throw new RuntimeException(
                    "Wrong number of constructor arguments for " + name
            );
        }

        constructor.invoke(env, object, args);
    }

    /**
     * Finds the single constructor declared by this class, rejecting classes
     * that declare more than one constructor.
     */
    public ConstructorDecl getConstructor() {
        ConstructorDecl result = null;

        for (Declaration declaration : declarations) {
            if (declaration instanceof ConstructorDecl constructor) {
                if (result != null) {
                    throw new TypeCheckException(
                            "Multiple constructors are not supported for class " + name
                    );
                }
                result = constructor;
            }
        }

        return result;
    }

    /**
     * Evaluates this class's own field declarations into the supplied object,
     * using an object-aware environment so initializers may reference fields.
     */
    private void initializeDeclaredFields(Environment env, ObjectValue object) {
        Environment objectEnv = new Environment(env);
        objectEnv.setThisObject(object);

        for (Declaration d : declarations) {
            if (d instanceof VarDecl v) {
                Value initVal = (v.initExpression != null)
                        ? v.initExpression.eval(objectEnv)
                        : defaultObjectFieldValue(env, v.type);

                initVal.label = v.label;
                object.fields.put(v.name, initVal);
                object.fieldLabels.put(v.name, v.label);
            }
        }
    }

    /**
     * Creates the default runtime value for an object field when no initializer
     * expression is present.
     */
    private Value defaultObjectFieldValue(Environment env, Types type) {
        if (type instanceof ClassType classType) {
            return env.instantiate(classType.name);
        }

        return JavaTypeSupport.defaultValue(type);
    }

    /**
     * Resolves a method declaration during type checking, walking up the
     * superclass chain when the method is inherited.
     */
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

    /**
     * Resolves a method declaration at runtime, including inherited methods.
     */
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

    /**
     * Resolves a field declaration during type checking, including inherited
     * fields from superclasses.
     */
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

    /**
     * Adds inherited field types and labels to the current class environment so
     * methods can type check inherited field accesses.
     */
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

    /**
     * Ensures the constructor declaration, when present, uses this class's name.
     */
    private void validateConstructors() {
        ConstructorDecl constructor = getConstructor();
        if (constructor != null && !name.equals(constructor.className)) {
            throw new TypeCheckException(
                    "Constructor name " + constructor.className
                            + " must match class " + name
            );
        }
    }
}
