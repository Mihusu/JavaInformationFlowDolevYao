// Generated from Information_flow.g4 by ANTLR 4.13.2
package antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link Information_flowParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface Information_flowVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(Information_flowParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#globalDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlobalDeclaration(Information_flowParser.GlobalDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#keyDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyDeclaration(Information_flowParser.KeyDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#formatDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormatDeclaration(Information_flowParser.FormatDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#class}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClass(Information_flowParser.ClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#classBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBlock(Information_flowParser.ClassBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration(Information_flowParser.DeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#methodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDeclaration(Information_flowParser.MethodDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#cmdBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmdBlock(Information_flowParser.CmdBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#decls}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecls(Information_flowParser.DeclsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#declItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclItem(Information_flowParser.DeclItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(Information_flowParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#basicType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasicType(Information_flowParser.BasicTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#encryptionType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEncryptionType(Information_flowParser.EncryptionTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(Information_flowParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(Information_flowParser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#sendStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSendStatement(Information_flowParser.SendStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#inputStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInputStatement(Information_flowParser.InputStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#receiveStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReceiveStatement(Information_flowParser.ReceiveStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(Information_flowParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#methodCallOrFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCallOrFormat(Information_flowParser.MethodCallOrFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(Information_flowParser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#assignmentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentStatement(Information_flowParser.AssignmentStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#lvalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLvalue(Information_flowParser.LvalueContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(Information_flowParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#elseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseStatement(Information_flowParser.ElseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#elseifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseifStatement(Information_flowParser.ElseifStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(Information_flowParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Information_flowParser#print}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrint(Information_flowParser.PrintContext ctx);
}