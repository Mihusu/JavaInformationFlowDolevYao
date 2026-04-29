package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

public class VarDecl extends Declaration {
    public Type type;
    public SecLabel label;
    public String name;
    public Expr init; // optional

    @Override
    public void eval(Environment env) {

        Value initVal;

        if (init != null) {
            initVal = init.eval(env);
        } else {
            initVal = defaultValue(type);
        }

        // assign security label
        initVal.label = label;

        // store in environment
        env.setVariables(name, initVal);
    }

    private Value defaultValue(Type t) {
        switch (t) {
            case INT: return new IntValue(0);
            case BOOL: return new BoolValue(false);
            case STRING: return new StringValue("");
        }
        throw new RuntimeException("Unknown type");
    }

    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {

        delta.putType(name, type);
        gamma.putLabel(name, label);

        if (init != null) {

            Type initType = init.typecheck(delta, gamma);

            if (initType != type) {
                throw new TypeCheckException(
                        "Type mismatch in initialization of " + name
                );
            }

            SecLabel initLabel = init.label(gamma);
            SecLabel effective = join(pc, initLabel);

            if (!Security.canFlow(effective, label)) {
                throw new TypeCheckException(
                        "Illegal flow in initialization of " + name
                );
            }
        }
    }

    private SecLabel join(SecLabel a, SecLabel b) {
        return (a == SecLabel.HIGH || b == SecLabel.HIGH)
                ? SecLabel.HIGH
                : SecLabel.LOW;
    }

    @Override
    public String compile(CodeGenEnv env) {
        return "";
    }
}
