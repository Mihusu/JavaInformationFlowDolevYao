    package ASTNodes;

    import Analysis.Environment;
    import Analysis.LabelEnv;
    import Analysis.TypeEnv;
    import CodeGeneration.CodeGenEnv;

    /**
     * Represents an expression in the AST.
     */
    abstract public class Expr extends Node {
        /**
         * Evaluates the expression in the given environment.
         * @param env The execution environment.
         * @return The resulting value.
         */
        abstract public Value eval(Environment env);

        /**
         * Performs label and type checking on the expression.
         * @param delta The type environment.
         * @param gamma The label environment.
         * @return The type of the expression.
         */
        abstract public Types typeChecker(TypeEnv delta, LabelEnv gamma);

        /**
         * Determines the security label of the expression.
         * @param gamma The label environment.
         * @return The security label.
         */
        abstract public SecLabel label(LabelEnv gamma);

        /**
         * Represents an integer literal.
         */
        public static class IntLiteral extends Expr {
            int value;

            public IntLiteral(int value) {
                this.value = value;
            }

            /**
             * Produces the runtime integer value represented by this literal.
             */
            public Value eval(Environment env) {
                return new IntValue(value);
            }

            @Override
            public Types typeChecker(TypeEnv delta, LabelEnv gamma) {
                return new BasicType(Type.INT);
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

        /**
         * Represents a boolean literal.
         */
        public static class BoolLiteral extends Expr {
            boolean value;

            public BoolLiteral(boolean b) {
                this.value = b;
            }

            /**
             * Produces the runtime boolean value represented by this literal.
             */
            public Value eval(Environment env) {
                return new BoolValue(value);
            }

            @Override
            public Types typeChecker(TypeEnv delta, LabelEnv gamma) {
                return new BasicType(Type.BOOL);
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

        /**
         * Represents a string literal.
         */
        public static class StringLiteral extends Expr {
            public String value;

            public StringLiteral(String value) {
                this.value = value;
            }

            /**
             * Produces the runtime string value represented by this literal.
             */
            public Value eval(Environment env) {
                return new StringValue(value);
            }

            @Override
            public Types typeChecker(TypeEnv delta, LabelEnv gamma) {
                return new BasicType(Type.STRING);
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
