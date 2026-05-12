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
	 * Enter a parse tree produced by {@link Information_flowParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDeclaration(Information_flowParser.FunctionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDeclaration(Information_flowParser.FunctionDeclarationContext ctx);
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
	 * Enter a parse tree produced by {@link Information_flowParser#format}.
	 * @param ctx the parse tree
	 */
	void enterFormat(Information_flowParser.FormatContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#format}.
	 * @param ctx the parse tree
	 */
	void exitFormat(Information_flowParser.FormatContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#formatList}.
	 * @param ctx the parse tree
	 */
	void enterFormatList(Information_flowParser.FormatListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#formatList}.
	 * @param ctx the parse tree
	 */
	void exitFormatList(Information_flowParser.FormatListContext ctx);
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
	 * Enter a parse tree produced by {@link Information_flowParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOr(Information_flowParser.LogicalOrContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOr(Information_flowParser.LogicalOrContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAnd(Information_flowParser.LogicalAndContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAnd(Information_flowParser.LogicalAndContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#equality}.
	 * @param ctx the parse tree
	 */
	void enterEquality(Information_flowParser.EqualityContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#equality}.
	 * @param ctx the parse tree
	 */
	void exitEquality(Information_flowParser.EqualityContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#relational}.
	 * @param ctx the parse tree
	 */
	void enterRelational(Information_flowParser.RelationalContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#relational}.
	 * @param ctx the parse tree
	 */
	void exitRelational(Information_flowParser.RelationalContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#additive}.
	 * @param ctx the parse tree
	 */
	void enterAdditive(Information_flowParser.AdditiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#additive}.
	 * @param ctx the parse tree
	 */
	void exitAdditive(Information_flowParser.AdditiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicative(Information_flowParser.MultiplicativeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicative(Information_flowParser.MultiplicativeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterUnary(Information_flowParser.UnaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitUnary(Information_flowParser.UnaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(Information_flowParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(Information_flowParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link Information_flowParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(Information_flowParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link Information_flowParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(Information_flowParser.FunctionCallContext ctx);
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