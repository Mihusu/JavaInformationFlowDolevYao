package Analysis;

import ASTNodes.*;
import Utils.Security;

import java.util.*;

/**
 * Represents the runtime environment for program execution.
 * Manages variables, security labels, methods, classes, communication channels, and cryptographic state.
 */
public class Environment {
    private final Environment parent;

    // Variables (σ)
    private final Map<String, Value> variables = new HashMap<>();

    // Security labels (runtime)
    public Map<String, SecLabel> labels = new HashMap<>();

    private final Map<String, MethodDecl> methods = new HashMap<>();
    private final Map<String, ClassDecl> classes = new HashMap<>();

    // Communication channels
    public Queue<Value> inbox = new LinkedList<>();
    public List<Value> outbox = new ArrayList<>();

    // Cryptographic randomness (CR)
    private final Map<String, Integer> cryptoCounter = new HashMap<>();
    private Value returnValue = null;
    public boolean hasReturned = false;
    private ObjectValue thisObject = null;

    // Observer level (intruder)
    public SecLabel observerLevel = SecLabel.LOW;

    /**
     * Creates the root runtime environment used when program execution begins.
     *
     * <p>
     * A root environment has no parent scope and owns the initial variable,
     * method, class, channel, and cryptographic state.
     * </p>
     */
    public Environment() {
        this.parent = null;
    }

    /**
     * Creates a child runtime environment with access to an enclosing scope.
     *
     * <p>
     * Child environments are used for method calls, constructor execution, and
     * object initialization. They keep their own local variables while sharing
     * global runtime structures such as classes, methods, and channels.
     * </p>
     *
     * @param parent Enclosing environment to use for fallback lookup.
     */
    public Environment(Environment parent) {
        this.parent = parent;
        this.labels.putAll(parent.labels);
        this.methods.putAll(parent.methods);
        this.classes.putAll(parent.classes);
        this.inbox = parent.inbox;
        this.outbox = parent.outbox;
        this.cryptoCounter.putAll(parent.cryptoCounter);
        this.observerLevel = parent.observerLevel;
        this.thisObject = parent.thisObject;
    }

    /**
     * Retrieves the value of a variable.
     * @param name The name of the variable.
     * @return The value of the variable.
     * @throws RuntimeException if the variable is undefined.
     */
    public Value getVariables(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }

        if (thisObject != null && thisObject.fields.containsKey(name)) {
            return thisObject.fields.get(name);
        }

        if (parent != null) {
            return parent.getVariables(name);
        }

        throw new RuntimeException("Undefined variable: " + name);
    }

    /**
     * Sets the value of an existing variable, checking for illegal information flow.
     * @param name The name of the variable.
     * @param value The new value to set.
     * @throws RuntimeException if information flow is illegal.
     */
    public void setVariables(String name, Value value) {
        if (!labels.containsKey(name) && thisObject != null && thisObject.fields.containsKey(name)) {
            setField(thisObject, name, value);
            return;
        }

        if (!labels.containsKey(name) && parent != null) {
            parent.setVariables(name, value);
            return;
        }

        SecLabel varLabel = labels.get(name);

        if (!Security.canFlow(value.label, varLabel)) {
            throw new RuntimeException(
                    "Illegal flow: " + value.label + " → " + varLabel
            );
        }

        variables.put(name, value);
    }

    /**
     * Declares a new variable with an initial value and security label.
     * @param name The name of the variable.
     * @param value The initial value.
     * @param label The security label.
     */
    public void declare(String name, Value value, SecLabel label) {
        variables.put(name, value);
        labels.put(name, label);
    }

    /**
     * Looks up the security label associated with a variable or object field.
     *
     * <p>
     * Lookup first checks local variable labels, then the active receiver
     * object, and finally the parent environment. A missing label returns
     * {@code null} so callers can decide how to handle absent bindings.
     * </p>
     *
     * @param name Variable or field name.
     * @return Security label for {@code name}, or {@code null} if unknown.
     */
    public SecLabel getLabel(String name) {
        if (labels.containsKey(name)) {
            return labels.get(name);
        }

        if (thisObject != null && thisObject.fieldLabels.containsKey(name)) {
            return thisObject.fieldLabels.get(name);
        }

        if (parent != null) {
            return parent.getLabel(name);
        }

        return null;
    }

    /**
     * Sets the active receiver object for object-aware execution.
     *
     * <p>
     * The receiver is also declared as {@code this} with a low label, allowing
     * method bodies and field initializers to refer to the current object.
     * </p>
     *
     * @param object Object currently being initialized or executing a method.
     */
    public void setThisObject(ObjectValue object) {
        this.thisObject = object;
        declare("this", object, SecLabel.LOW);
    }

    /**
     * Updates a field on an object after checking the field label.
     *
     * <p>
     * Runtime assignment must respect information flow: the value being stored
     * may only flow to the declared security label of the target field.
     * </p>
     *
     * @param object Object containing the field.
     * @param name Field name.
     * @param value New field value.
     * @throws RuntimeException if the field is unknown or the flow is illegal.
     */
    public void setField(ObjectValue object, String name, Value value) {
        if (!object.fieldLabels.containsKey(name)) {
            throw new RuntimeException("Unknown field " + name + " on " + object.className);
        }

        SecLabel fieldLabel = object.fieldLabels.get(name);
        if (!Security.canFlow(value.label, fieldLabel)) {
            throw new RuntimeException("Illegal flow: " + value.label + " â†’ " + fieldLabel);
        }

        object.fields.put(name, value);
    }

    /**
     * Retrieves a field value from an object.
     *
     * @param object Object containing the field.
     * @param name Field name.
     * @return Runtime value stored in the field.
     * @throws RuntimeException if the field is unknown for the object.
     */
    public Value getField(ObjectValue object, String name) {
        if (!object.fields.containsKey(name)) {
            throw new RuntimeException("Unknown field " + name + " on " + object.className);
        }

        return object.fields.get(name);
    }

    /**
     * Registers a method declaration in the runtime environment.
     *
     * @param name Method name.
     * @param f Method declaration to store.
     */
    public void putMethod(String name, MethodDecl f) {
        methods.put(name, f);
    }

    /**
     * Retrieves a globally registered method declaration.
     *
     * @param name Method name.
     * @return Registered method declaration.
     * @throws RuntimeException if no method with {@code name} exists.
     */
    public MethodDecl getMethod(String name) {
        if (!methods.containsKey(name)) {
            throw new RuntimeException("Unknown method: " + name);
        }
        return methods.get(name);
    }

    /**
     * Registers a class declaration so objects can be instantiated at runtime.
     *
     * @param cls Class declaration to register.
     */
    public void putClass(ClassDecl cls) {
        classes.put(cls.name, cls);
    }

    /**
     * Retrieves a class declaration by name.
     *
     * @param name Class name.
     * @return Registered class declaration.
     * @throws RuntimeException if the class is unknown.
     */
    public ClassDecl getClassDecl(String name) {
        if (!classes.containsKey(name)) {
            throw new RuntimeException("Unknown class: " + name);
        }

        return classes.get(name);
    }

    /**
     * Instantiates a class using an empty constructor argument list.
     *
     * @param className Class to instantiate.
     * @return Fresh object value initialized by the class declaration.
     */
    public ObjectValue instantiate(String className) {
        return instantiate(className, List.of());
    }

    /**
     * Instantiates a class and initializes it with constructor arguments.
     *
     * <p>
     * Class initialization handles inherited fields first, then local fields,
     * and finally invokes the matching constructor body when one is declared.
     * </p>
     *
     * @param className Class to instantiate.
     * @param args Runtime constructor arguments.
     * @return Fresh initialized object value.
     */
    public ObjectValue instantiate(String className, List<Value> args) {
        ClassDecl cls = getClassDecl(className);
        ObjectValue object = new ObjectValue(className);
        cls.initializeInstance(this, object, args);
        return object;
    }

    /**
     * Resolves a method on a class, including inherited methods.
     *
     * @param className Class on which the method is invoked.
     * @param methodName Method name to resolve.
     * @return Method declaration found in the class or one of its superclasses.
     * @throws RuntimeException if the method cannot be found.
     */
    public MethodDecl resolveMethod(String className, String methodName) {
        ClassDecl cls = getClassDecl(className);
        MethodDecl method = cls.findMethod(methodName, this);
        if (method == null) {
            throw new RuntimeException("Unknown method " + methodName + " on " + className);
        }
        return method;
    }

    /**
     * Retrieves the value produced by the current method return.
     *
     * @return Runtime value stored by the most recent return statement.
     * @throws RuntimeException if no return value has been set.
     */
    public Value getReturnValue() {
        if (!hasReturned) {
            throw new RuntimeException("No return value set");
        }
        return returnValue;
    }

    /**
     * Records a method return value and marks the current call as returned.
     *
     * @param value Runtime value produced by a return statement.
     */
    public void setReturnValue(Value value) {
        this.returnValue = value;
        this.hasReturned = true;
    }
}
