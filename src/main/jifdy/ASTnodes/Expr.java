    package ASTnodes;

    import Analysis.Environment;
    import Analysis.LabelEnv;
    import Analysis.TypeEnv;
    import CodeGeneration.CodeGenEnv;

    abstract public class Expr extends Node {
        abstract public Value eval(Environment env);
        abstract public Type typecheck(TypeEnv delta, LabelEnv gamma);
        abstract public SecLabel label(LabelEnv gamma);

        public static class IntLiteral extends Expr {
            int value;

            public IntLiteral(int value) {
                this.value = value;
            }

            public Value eval(Environment env) {
                return new IntValue(value);
            }

            @Override
            public Type typecheck(TypeEnv delta, LabelEnv gamma) {
                return Type.INT;
            }

            @Override
            public SecLabel label(LabelEnv gamma) {
                return SecLabel.LOW;
            }

            @Override
            public String compile(CodeGenEnv env) {
                return Integer.toString(value);
            }
        }

        public static class BoolLiteral extends Expr {
            boolean value;

            public BoolLiteral(boolean b) {
                this.value = b;
            }

            public Value eval(Environment env) {
                return new BoolValue(value);
            }

            @Override
            public Type typecheck(TypeEnv delta, LabelEnv gamma) {
                return Type.BOOL;
            }

            @Override
            public SecLabel label(LabelEnv gamma) {
                return SecLabel.LOW;
            }

            @Override
            public String compile(CodeGenEnv env) {
                return Boolean.toString(value);
            }
        }

        public static class StringLiteral extends Expr {
            public String value;

            public StringLiteral(String value) {
                this.value = value;
            }

            public Value eval(Environment env) {
                return new StringValue(value);
            }

            @Override
            public Type typecheck(TypeEnv delta, LabelEnv gamma) {
                return Type.STRING;
            }

            @Override
            public SecLabel label(LabelEnv gamma) {
                return SecLabel.LOW;
            }

            @Override
            public String compile(CodeGenEnv env) {
                return "\"" + value + "\"";
            }
        }

        @Override
        public abstract String compile(CodeGenEnv env);
    }