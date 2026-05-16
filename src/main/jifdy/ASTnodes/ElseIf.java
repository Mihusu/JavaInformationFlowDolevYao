package ASTnodes;

/**
 * Data holder for a single {@code else if} branch inside an {@link IfStmt}.
 */
public class ElseIf {
    public Expr condition;
    public CmdBlock cmdBlock;
}
