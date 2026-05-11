package Analysis;

import ASTnodes.SecLabel;

public class Security {

    public static boolean canFlow(SecLabel from, SecLabel to) {
        if (from == SecLabel.HIGH && to == SecLabel.LOW)
            return false;
        return true;
    }
}