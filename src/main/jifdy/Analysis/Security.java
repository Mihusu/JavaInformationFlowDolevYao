package Analysis;

import ASTnodes.SecLabel;

/**
 * Utility class for security label operations.
 */
public class Security {

    /**
     * Determines if information is allowed to flow between two security labels.
     * @param from The source security label.
     * @param to The target security label.
     * @return true if flow is allowed, false if it's an illegal flow (e.g., HIGH to LOW).
     */
    public static boolean canFlow(SecLabel from, SecLabel to) {
        if (from == SecLabel.HIGH && to == SecLabel.LOW)
            return false;
        return true;
    }
}