package ASTNodes;

import Analysis.*;
import CodeGeneration.CodeGenEnv;
import Utils.Security;
import Utils.TypeCheckException;

/**
 * Represents a variable declaration in the AST.
 */
public class VarDecl extends Declaration {
    /**
     * The type of the variable.
     */
    public Types type;

    /**
     * The security label of the variable.
     */
    public SecLabel label;

    /**
     * The name of the variable.
     */
    public String name;

    /**
     * The optional initializer expression.
     */
    public Expr initExpression; // optional

    /**
     * Evaluates the variable declaration, initializing it in the environment.
     * @param env The execution environment.
     */
    @Override
    public void eval(Environment env) {

        Value initVal;

        if (initExpression != null) {
            initVal = initExpression.eval(env);
        } else if (type instanceof ClassType classType) {
            initVal = env.instantiate(classType.name);
        } else {
            initVal = JavaTypeSupport.defaultValue(type);
        }

        // assign security label
        initVal.label = label;

        // store in environment
        env.declare(name, initVal, label);
    }

    /**
     * Performs type checking on the variable declaration.
     * @param delta The type environment.
     * @param gamma The label environment.
     * @param variableProcedure The security context (pc).
     */
    @Override
    public void typecheck(TypeEnv delta, LabelEnv gamma, SecLabel variableProcedure) {

        delta.putType(name, type);
        gamma.putLabel(name, label);

        if (initExpression != null) {

            Types initType = initExpression.typecheck(delta, gamma);

            if (!delta.isSubtype(initType, type)) {
                throw new TypeCheckException(
                        "Type mismatch in initialization of " + name,
                        lineNumber,
                        name
                );
            }

            SecLabel initLabel = initExpression.label(gamma);
            SecLabel effective = SecLabel.join(variableProcedure, initLabel);

            // SPECIAL CASE: Encryption (EncryptExpr) is a declassification mechanism.
            // It results in a LOW ciphertext even if the payload or context is HIGH.
            // The user explicitly requested that encryption doesn't cause illegal flow.
            if (!Security.canFlow(effective, label)) {
                throw new TypeCheckException(
                        "Illegal flow in initialization of " + name,
                        lineNumber,
                        name
                );
            }
        }
    }


    @Override
    public String compile(CodeGenEnv env) {
        env.declareVariable(name, type);
        return env.indent() + JavaTypeSupport.toJavaType(type) + " " + name + compileInitializer(env) + ";\n";
    }

    public String compileField(CodeGenEnv env) {
        env.declareVariable(name, type);
        return env.indent() + "public " + JavaTypeSupport.toJavaType(type) + " " + name + compileInitializer(env) + ";\n";
    }

    private String compileInitializer(CodeGenEnv env) {
        if (initExpression != null) {
            return " = " + initExpression.compile(env);
        }

        if (type instanceof ClassType classType) {
            return " = new " + classType.name + "()";
        }

        return " = " + JavaTypeSupport.defaultValueExpression(type);
    }
}
