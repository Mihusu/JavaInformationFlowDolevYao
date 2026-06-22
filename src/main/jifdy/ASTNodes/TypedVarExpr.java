package ASTNodes;

import Analysis.Environment;
import Analysis.LabelEnv;
import Utils.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;

/**
 * Variable read with an asserted type and security label from syntax such as
 * `String low user`. It evaluates/compiles exactly like `user`, but typechecking
 * verifies that the annotation matches the existing variable.
 */
public class TypedVarExpr extends Expr {
    public final String name;
    public final Types assertedType;
    public final SecLabel assertedLabel;

    public TypedVarExpr(String name, Types assertedType, SecLabel assertedLabel) {
        this.name = name;
        this.assertedType = assertedType;
        this.assertedLabel = assertedLabel;
    }

    @Override
    public Value eval(Environment env) {
        return env.getVariables(name);
    }

    @Override
    public Types typecheck(TypeEnv delta, LabelEnv gamma) {
        // The syntax carries a type/label annotation, but this is still only a
        // read of an already-declared variable. Reject stale or misleading annotations.
        Types actualType = delta.getType(name);
        if (!Types.sameType(actualType, assertedType)) {
            throw new TypeCheckException("Typed reference type mismatch for " + name, lineNumber, name);
        }

        SecLabel actualLabel = gamma.getLabel(name);
        if (actualLabel != assertedLabel) {
            throw new TypeCheckException("Typed reference label mismatch for " + name, lineNumber, name);
        }

        return actualType;
    }

    @Override
    public SecLabel label(LabelEnv gamma) {
        // Re-check the asserted label here because label(...) can be used independently
        // during information-flow checks after typecheck(...) has run.
        SecLabel actualLabel = gamma.getLabel(name);
        if (actualLabel != assertedLabel) {
            throw new TypeCheckException("Typed reference label mismatch for " + name, lineNumber, name);
        }
        return actualLabel;
    }

    @Override
    public String compile(CodeGenEnv env) {
        return name;
    }
}
