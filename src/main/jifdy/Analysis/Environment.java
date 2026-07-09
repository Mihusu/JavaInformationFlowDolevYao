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

    public Environment() {
        this.parent = null;
    }

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

    public void setThisObject(ObjectValue object) {
        this.thisObject = object;
        declare("this", object, SecLabel.LOW);
    }

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

    public Value getField(ObjectValue object, String name) {
        if (!object.fields.containsKey(name)) {
            throw new RuntimeException("Unknown field " + name + " on " + object.className);
        }

        return object.fields.get(name);
    }

    public void putMethod(String name, MethodDecl f) {
        methods.put(name, f);
    }

    public MethodDecl getMethod(String name) {
        if (!methods.containsKey(name)) {
            throw new RuntimeException("Unknown method: " + name);
        }
        return methods.get(name);
    }

    public void putClass(ClassDecl cls) {
        classes.put(cls.name, cls);
    }

    public ClassDecl getClassDecl(String name) {
        if (!classes.containsKey(name)) {
            throw new RuntimeException("Unknown class: " + name);
        }

        return classes.get(name);
    }

    public ObjectValue instantiate(String className) {
        return instantiate(className, List.of());
    }

    public ObjectValue instantiate(String className, List<Value> args) {
        ClassDecl cls = getClassDecl(className);
        ObjectValue object = new ObjectValue(className);
        cls.initializeInstance(this, object, args);
        return object;
    }

    public MethodDecl resolveMethod(String className, String methodName) {
        ClassDecl cls = getClassDecl(className);
        MethodDecl method = cls.findMethod(methodName, this);
        if (method == null) {
            throw new RuntimeException("Unknown method " + methodName + " on " + className);
        }
        return method;
    }

    public Value getReturnValue() {
        if (!hasReturned) {
            throw new RuntimeException("No return value set");
        }
        return returnValue;
    }

    public void setReturnValue(Value value) {
        this.returnValue = value;
        this.hasReturned = true;
    }
}
