package ASTNodes;

public class CiphertextType extends Types {

    public String keyName;
    public String formatName;

    public CiphertextType(String keyName,
                          String formatName) {

        super(keyName, formatName);
        this.keyName = keyName;
        this.formatName = formatName;
    }

    @Override
    public String toString() {

        return "e@" +
                keyName +
                "(" +
                formatName +
                ")";
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CiphertextType other)) {
            return false;
        }

        return keyName.equals(other.keyName)
                && formatName.equals(other.formatName);
    }

    @Override
    public int hashCode() {
        return 31 * keyName.hashCode() + formatName.hashCode();
    }
}
