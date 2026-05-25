package ASTnodes;

public interface Operators {

    static Type runtimeType(Operators operator) {
        if (operator instanceof Type type) {
            return type;
        }

        if (operator instanceof BasicType basicType) {
            return basicType.type;
        }

        if (operator instanceof CiphertextType) {
            return Type.CIPHERTEXT;
        }

        if (operator instanceof FormatType) {
            return Type.FORMAT;
        }

        throw new RuntimeException("Unknown operator type: " + operator);
    }

    static boolean sameType(Operators left, Operators right) {
        if (left == right) {
            return true;
        }

        if (left == null || right == null) {
            return false;
        }

        if (left instanceof CiphertextType leftCipher && right instanceof CiphertextType rightCipher) {
            return leftCipher.equals(rightCipher);
        }

        if (left instanceof FormatType leftFormat && right instanceof FormatType rightFormat) {
            return leftFormat.equals(rightFormat);
        }

        return runtimeType(left) == runtimeType(right);
    }
}
