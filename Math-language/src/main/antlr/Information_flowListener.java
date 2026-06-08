// Generated from C:/Users/ming2/GitHub/Java_information_flow_dolev_yao/src/main/jifdy/Information_flow.g4 by ANTLR 4.13.2
package antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link Information_flowParser}.
 */
public interface Information_flowListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(Information_flowParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(Information_flowParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#globalDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterGlobalDeclaration(Information_flowParser.GlobalDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#globalDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitGlobalDeclaration(Information_flowParser.GlobalDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#keyDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterKeyDeclaration(Information_flowParser.KeyDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#keyDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitKeyDeclaration(Information_flowParser.KeyDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#formatDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFormatDeclaration(Information_flowParser.FormatDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#formatDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFormatDeclaration(Information_flowParser.FormatDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#class}.
	 * @param ctx the parse tree
	 */
	void enterClass(Information_flowParser.ClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#class}.
	 * @param ctx the parse tree
	 */
	void exitClass(Information_flowParser.ClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#classBlock}.
	 * @param ctx the parse tree
	 */
	void enterClassBlock(Information_flowParser.ClassBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#classBlock}.
	 * @param ctx the parse tree
	 */
	void exitClassBlock(Information_flowParser.ClassBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(Information_flowParser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(Information_flowParser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMethodDeclaration(Information_flowParser.MethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMethodDeclaration(Information_flowParser.MethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#cmdBlock}.
	 * @param ctx the parse tree
	 */
	void enterCmdBlock(Information_flowParser.CmdBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#cmdBlock}.
	 * @param ctx the parse tree
	 */
	void exitCmdBlock(Information_flowParser.CmdBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#decls}.
	 * @param ctx the parse tree
	 */
	void enterDecls(Information_flowParser.DeclsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#decls}.
	 * @param ctx the parse tree
	 */
	void exitDecls(Information_flowParser.DeclsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#declItem}.
	 * @param ctx the parse tree
	 */
	void enterDeclItem(Information_flowParser.DeclItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#declItem}.
	 * @param ctx the parse tree
	 */
	void exitDeclItem(Information_flowParser.DeclItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(Information_flowParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(Information_flowParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#basicType}.
	 * @param ctx the parse tree
	 */
	void enterBasicType(Information_flowParser.BasicTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#basicType}.
	 * @param ctx the parse tree
	 */
	void exitBasicType(Information_flowParser.BasicTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#encryptionType}.
	 * @param ctx the parse tree
	 */
	void enterEncryptionType(Information_flowParser.EncryptionTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#encryptionType}.
	 * @param ctx the parse tree
	 */
	void exitEncryptionType(Information_flowParser.EncryptionTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(Information_flowParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(Information_flowParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(Information_flowParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(Information_flowParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#sendStatement}.
	 * @param ctx the parse tree
	 */
	void enterSendStatement(Information_flowParser.SendStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#sendStatement}.
	 * @param ctx the parse tree
	 */
	void exitSendStatement(Information_flowParser.SendStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#inputStatement}.
	 * @param ctx the parse tree
	 */
	void enterInputStatement(Information_flowParser.InputStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#inputStatement}.
	 * @param ctx the parse tree
	 */
	void exitInputStatement(Information_flowParser.InputStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#receiveStatement}.
	 * @param ctx the parse tree
	 */
	void enterReceiveStatement(Information_flowParser.ReceiveStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#receiveStatement}.
	 * @param ctx the parse tree
	 */
	void exitReceiveStatement(Information_flowParser.ReceiveStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(Information_flowParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(Information_flowParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#methodCallOrFormat}.
	 * @param ctx the parse tree
	 */
	void enterMethodCallOrFormat(Information_flowParser.MethodCallOrFormatContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#methodCallOrFormat}.
	 * @param ctx the parse tree
	 */
	void exitMethodCallOrFormat(Information_flowParser.MethodCallOrFormatContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentList(Information_flowParser.ArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentList(Information_flowParser.ArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentStatement(Information_flowParser.AssignmentStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentStatement(Information_flowParser.AssignmentStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void enterLvalue(Information_flowParser.LvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void exitLvalue(Information_flowParser.LvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(Information_flowParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(Information_flowParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#elseStatement}.
	 * @param ctx the parse tree
	 */
	void enterElseStatement(Information_flowParser.ElseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#elseStatement}.
	 * @param ctx the parse tree
	 */
	void exitElseStatement(Information_flowParser.ElseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#elseifStatement}.
	 * @param ctx the parse tree
	 */
	void enterElseifStatement(Information_flowParser.ElseifStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#elseifStatement}.
	 * @param ctx the parse tree
	 */
	void exitElseifStatement(Information_flowParser.ElseifStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(Information_flowParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(Information_flowParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#print}.
	 * @param ctx the parse tree
	 */
	void enterPrint(Information_flowParser.PrintContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#print}.
	 * @param ctx the parse tree
	 */
	void exitPrint(Information_flowParser.PrintContext ctx);
}