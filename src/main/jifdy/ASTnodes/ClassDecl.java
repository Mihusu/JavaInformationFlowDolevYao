package ASTnodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;

import java.util.ArrayList;
import java.util.List;

public class ClassDecl extends Node {
    public Privacy privacy; // optional
    public String name;
    public List<Declaration> declarations;
    public List<FunctionDecl> functions;

    public void eval(Environment env) {

        // 1. Initialize variables
        for (Declaration d : declarations) {
            if (d instanceof VarDecl) {
                VarDecl v = (VarDecl) d;

                Value initVal = (v.init != null)
                        ? v.init.eval(env)
                        : defaultValue(v.type);

                initVal.label = v.label;

                env.labels.put(v.name, v.label);
                env.setVariables(v.name, initVal);
            }
        }
    }

    @Override
    public String compile(CodeGenEnv env) {

        StringBuilder sb = new StringBuilder();

        for (Node d : declarations) {
            sb.append(d.compile(env));
        }

        return sb.toString();
    }

    public void typecheck(TypeEnv delta, LabelEnv gamma) {

        // First: register variables
        for (Declaration d : declarations) {

            if (d instanceof VarDecl v) {

                // Add variable to environments
                delta.putType(v.name, v.type);
                gamma.putLabel(v.name, v.label);

                // If initialized, check the initializer
                if (v.init != null) {

                    Type initType = v.init.typecheck(delta, gamma);

                    if (initType != v.type) {
                        throw new TypeCheckException(
                                "Type mismatch in declaration of " + v.name
                        );
                    }

                    SecLabel initLabel = v.init.label(gamma);

                    if (!Security.canFlow(initLabel, v.label)) {
                        throw new TypeCheckException(
                                "Illegal information flow in initialization of "
                                        + v.name + ": "
                                        + initLabel + " -> " + v.label
                        );
                    }
                }
            }
        }

        // Then: register function signatures first
        for (FunctionDecl f : functions) {

            List<Type> paramTypes = new ArrayList<>();
            List<SecLabel> paramLabels = new ArrayList<>();

            for (Param p : f.params) {
                paramTypes.add(p.type);
                paramLabels.add(p.label);
            }

            delta.putFunction(
                    f.name,
                    new FunctionType(paramTypes, f.returnType, paramLabels, f.returnLabel)
            );

            gamma.putFunction(
                    f.name,
                    new FunctionLabel(paramLabels, f.returnLabel)
            );
        }

        // Finally: typecheck function bodies
        for (FunctionDecl f : functions) {
            f.typecheck(delta, gamma, SecLabel.LOW);
        }
    }

    private Value defaultValue(Type t) {
        switch (t) {
            case INT: return new IntValue(0);
            case BOOL: return new BoolValue(false);
            case STRING: return new StringValue("");
            case CIPHERTEXT: return new EncryptedValue("", new StringValue(""), "");
        }
        throw new TypeCheckException("Unknown type");
    }
}
