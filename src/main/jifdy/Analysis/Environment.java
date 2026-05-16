package Analysis;

import ASTnodes.*;

import java.util.*;

/**
 * Represents the runtime environment for program execution.
 * Manages variables, security labels, procedures, functions, communication channels, and cryptographic state.
 */
public class Environment {

    // Variables (σ)
    private final Map<String, Value> variables = new HashMap<>();

    // Security labels (runtime)
    public Map<String, SecLabel> labels = new HashMap<>();

    // Procedures (functions)
    private Map<String, ProcDecl> procedures = new HashMap<>();
    private final Map<String, FunctionDecl> functions = new HashMap<>();


    // Communication channels
    public Queue<Value> inbox = new LinkedList<>();
    public List<Value> outbox = new ArrayList<>();

    // Cryptographic randomness (CR)
    private final Map<String, Integer> cryptoCounter = new HashMap<>();
    private Value returnValue = null;
    public boolean hasReturned = false;

    // Observer level (intruder)
    public SecLabel observerLevel = SecLabel.LOW;

    // =========================
    // VARIABLES
    // =========================

    /**
     * Retrieves the value of a variable.
     * @param name The name of the variable.
     * @return The value of the variable.
     * @throws RuntimeException if the variable is undefined.
     */
    public Value getVariables(String name) {
        if (!variables.containsKey(name))
            throw new RuntimeException("Undefined variable: " + name);
        return variables.get(name);
    }

    /**
     * Sets the value of an existing variable, checking for illegal information flow.
     * @param name The name of the variable.
     * @param value The new value to set.
     * @throws RuntimeException if information flow is illegal.
     */
    public void setVariables(String name, Value value) {
        SecLabel varLabel = labels.get(name);

        if (!canFlow(value.label, varLabel)) {
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
        return labels.get(name);
    }

    // =========================
    // PROCEDURES
    // =========================

    public void putProcedure(String name, ProcDecl proc) {
        procedures.put(name, proc);
    }

    public ProcDecl getProcedure(String name) {
        if (!procedures.containsKey(name))
            throw new RuntimeException("Undefined procedure: " + name);
        return procedures.get(name);
    }

    public void callProcedure(String name, List<Expr> args) {

        ProcDecl proc = getProcedure(name);

        // create local environment (IMPORTANT)
        Environment localEnv = new Environment();

        // copy global state if needed (optional for now)
        localEnv.procedures = this.procedures;

        // bind parameters
        for (int i = 0; i < proc.params.size(); i++) {

            String paramName = proc.params.get(i).name;
            Value argValue = args.get(i).eval(this);

            localEnv.declare(paramName, argValue, argValue.label);
        }

        // execute body
        for (Stmt stmt : proc.body) {
            stmt.eval(localEnv);
        }
    }

    // =========================
    // FUNCTIONS
    // =========================

    public void putFunction(String name, FunctionDecl f) {
        functions.put(name, f);
    }

    public FunctionDecl getFunction(String name) {
        if (!functions.containsKey(name)) {
            throw new RuntimeException("Unknown function: " + name);
        }
        return functions.get(name);
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

    // =========================
    // CRYPTO
    // =========================

    public int nextCryptoCounter(String key) {
        int c = cryptoCounter.getOrDefault(key, 0);
        cryptoCounter.put(key, c + 1);
        return c;
    }

    // =========================
    // SECURITY
    // =========================

    /**
     * Checks if information can flow from one security label to another.
     * @param from The source security label.
     * @param to The target security label.
     * @return true if the flow is allowed, false otherwise.
     */
    public boolean canFlow(SecLabel from, SecLabel to) {
        return !(from == SecLabel.HIGH && to == SecLabel.LOW);
    }
}