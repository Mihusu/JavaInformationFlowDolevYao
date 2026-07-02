package ASTNodes;

import ASTBuilder.PublicPrivateLabel;
import Analysis.Environment;
import Analysis.LabelEnv;
import Utils.TypeCheckException;
import Analysis.TypeEnv;
import CodeGeneration.CodeGenEnv;
import Utils.Security;

import java.util.List;

/**
 * Constructor declaration used to initialize a newly created class instance.
 */
public class ConstructorDecl extends Declaration {
    public PublicPrivateLabel publicPrivateLabel;
    public String className;
    public List<Param> params;
    public List<Stmt> body;

    @Override
    public void eval(Environment env) {
        // Constructors execute only when an object is instantiated.
    }

    @Override
    public void labelTypeCheck(TypeEnv delta, LabelEnv gamma, SecLabel pc) {
        TypeEnv localDelta = new TypeEnv(delta);
        LabelEnv localGamma = new LabelEnv(gamma);

        for (Param param : params) {
            localDelta.putType(param.name, param.type);
            localGamma.putLabel(param.name, param.label);
        }

        for (Stmt statement : body) {
            statement.labelTypeChecker(localDelta, localGamma, SecLabel.LOW);
        }
    }

    @Override
    public String compile(CodeGenEnv env) {
        StringBuilder sb = new StringBuilder();

        sb.append(env.indent())
                .append("public ")
                .append(className)
                .append("(");

        for (int i = 0; i < params.size(); i++) {
            Param param = params.get(i);
            sb.append(JavaTypeSupport.toJavaType(param.type))
                    .append(" ")
                    .append(param.name);

            if (i < params.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append(") {\n");

        env.pushScope();
        for (Param param : params) {
            env.declareVariable(param.name, param.type);
        }

        env.increaseIndent();
        for (Stmt statement : body) {
            sb.append(statement.compile(env));
        }
        env.decreaseIndent();
        env.popScope();

        sb.append(env.indent()).append("}\n");
        return sb.toString();
    }

    public void checkArguments(List<Expr> args, TypeEnv delta, LabelEnv gamma) {
        if (args.size() != params.size()) {
            throw new TypeCheckException(
                    "Wrong number of constructor arguments for " + className
            );
        }

        for (int i = 0; i < args.size(); i++) {
            Types actualType = args.get(i).labelTypeCheck(delta, gamma);
            Param expected = params.get(i);

            if (!delta.isSubtype(actualType, expected.type)) {
                throw new TypeCheckException(
                        "Constructor argument type mismatch for " + className
                );
            }

            SecLabel actualLabel = args.get(i).label(gamma);
            if (!Security.canFlow(actualLabel, expected.label)) {
                throw new TypeCheckException(
                        "Illegal constructor argument flow for " + className + ": "
                                + actualLabel + " -> " + expected.label
                );
            }
        }
    }

    public void invoke(Environment env, ObjectValue object, List<Value> args) {
        Environment constructorEnv = new Environment(env);
        constructorEnv.setThisObject(object);

        for (int i = 0; i < params.size(); i++) {
            Param param = params.get(i);
            constructorEnv.declare(param.name, args.get(i), param.label);
        }

        for (Stmt statement : body) {
            statement.eval(constructorEnv);
        }
    }
}
