package ASTBuilder;

import ASTnodes.*;
import Analysis.TypeCheckException;
import antlr.Information_flowBaseVisitor;
import antlr.Information_flowParser;
import ASTnodes.Expr.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ANTLR visitor that lowers parse-tree nodes into the project's AST representation.
 */
public class ASTBuilder extends Information_flowBaseVisitor<Node> {

    private final Set<String> declaredKeys = new HashSet<>();
    private final Set<String> declaredFormats = new HashSet<>();
    private final Map<String, FormatType> declaredFormatTypes = new LinkedHashMap<>();

    // =========================
    // PROGRAM / CLASS
    // =========================

    @Override
    public Node visitProgram(Information_flowParser.ProgramContext ctx) {
        for (Information_flowParser.GlobalDeclarationContext globalCtx : ctx.globalDeclaration()) {
            if (globalCtx.keyDeclaration() != null) {
                declareKey(globalCtx.keyDeclaration());
            } else if (globalCtx.formatDeclaration() != null) {
                declareFormat(globalCtx.formatDeclaration());
            }
        }

        ClassDecl cls = (ClassDecl) visit(ctx.class_());
        return setLocation(new Program(
                List.of(cls),
                new HashSet<>(declaredKeys),
                new HashSet<>(declaredFormats),
                new LinkedHashMap<>(declaredFormatTypes)
        ), ctx);
    }

    @Override
    public Node visitClass(Information_flowParser.ClassContext ctx) {

        Privacy privacy = null;
        if (ctx.PPLABEL() != null) {
            privacy = parsePrivacy(ctx.PPLABEL().getText());
        }

        ClassDecl cls = (ClassDecl) visit(ctx.classBlock());
        cls.privacy = privacy;

        return setLocation(cls, ctx);
    }

    @Override
    public Node visitClassBlock(Information_flowParser.ClassBlockContext ctx) {

        ClassDecl cls = new ClassDecl();
        cls.name = ctx.IDENTIFIER().getText();

        cls.declarations = new ArrayList<>();
        cls.methods = new ArrayList<>();
        cls.statements = new ArrayList<>();

        for (Information_flowParser.DeclarationContext d : ctx.declaration()) {
            cls.declarations.add((Declaration) visit(d));
        }

        if (ctx.methodDeclaration() != null) {
            for (Information_flowParser.MethodDeclarationContext fctx : ctx.methodDeclaration()) {
                cls.methods.add((MethodDecl) visit(fctx));
            }
        }

        if (ctx.statement() != null) {
            for (Information_flowParser.StatementContext sctx : ctx.statement()) {
                cls.statements.add((Stmt) visit(sctx));
            }
        }

        return setLocation(cls, ctx);
    }

    // =========================
    // DECLARATIONS
    // =========================

    @Override
    public Node visitMethodDeclaration(Information_flowParser.MethodDeclarationContext ctx) {

        MethodDecl f = new MethodDecl();

        // privacy
        f.privacy = parsePrivacy(ctx.PPLABEL().getText());

        // return type
        if (ctx.type() != null) {
            f.returnType = parseType(ctx.type());
        } else {
            f.returnType = null; // void
        }

        // parse security label
        f.returnLabel = parseSecLabel(ctx.SECLABEL().getText());

        // name
        f.name = ctx.IDENTIFIER().getText();

        // parameters
        f.params = new ArrayList<>();
        if (ctx.decls() != null) {
            for (Information_flowParser.DeclsContext dctx : ctx.decls()) {
                f.params.addAll(buildParams(dctx));
            }
        }

        // body
        f.body = (CmdBlock) visit(ctx.cmdBlock());

        return setLocation(f, ctx);
    }

    @Override
    public Node visitDeclaration(Information_flowParser.DeclarationContext ctx) {

        if (ctx.PPLABEL() == null) {

            VarDecl decl = new VarDecl();

            // Primitive, format, and ciphertext declarations now share one grammar form.
            decl.type = parseType(ctx.type());
            decl.name = ctx.IDENTIFIER().getText();

            // Variables always have an explicit security label in the grammar.
            decl.label = parseSecLabel(ctx.SECLABEL().getText());

            if (ctx.expression() != null) {
                decl.init = (Expr) visit(ctx.expression());
            }

            return setLocation(decl, ctx);
        }

        // Procedure declaration
        ProcDecl proc = new ProcDecl();
        proc.privacy = parsePrivacy(ctx.PPLABEL().getText());
        proc.name = ctx.IDENTIFIER().getText();

        proc.params = new ArrayList<>();
        for (Information_flowParser.DeclsContext dctx : ctx.decls()) {
            proc.params.addAll(buildParams(dctx));
        }
        proc.body = new ArrayList<>();

        for (Information_flowParser.AssignmentStatementContext stmt : ctx.assignmentStatement()) {
            proc.body.add((Stmt) visit(stmt));
        }

        return setLocation(proc, ctx);
    }

    // =========================
    // BLOCK
    // =========================

    @Override
    public CmdBlock visitCmdBlock(Information_flowParser.CmdBlockContext ctx) {

        CmdBlock block = new CmdBlock();
        block.statements = new ArrayList<>();

        for (ParseTree child : ctx.children) {
            if (child instanceof Information_flowParser.DeclarationContext d) {
                block.statements.add(visit(d));
            } else if (child instanceof Information_flowParser.StatementContext s) {
                block.statements.add(visit(s));
            } else if (child instanceof Information_flowParser.CmdBlockContext b) {
                block.statements.add(visit(b));
            }
        }

        return setLocation(block, ctx);
    }

    // =========================
    // STATEMENTS
    // =========================

    @Override
    public Node visitStatement(Information_flowParser.StatementContext ctx) {
        if (ctx.assignmentStatement() != null) return visit(ctx.assignmentStatement());
        if (ctx.methodCallOrFormat() != null) return new MethodCallStmt((Expr) visit(ctx.methodCallOrFormat()));
        if (ctx.sendStatement() != null) return visit(ctx.sendStatement());
        if (ctx.receiveStatement() != null) return visit(ctx.receiveStatement());
        if (ctx.returnStatement() != null) return visit(ctx.returnStatement());
        if (ctx.ifStatement() != null) return visit(ctx.ifStatement());
        if (ctx.whileStatement() != null) return visit(ctx.whileStatement());
        if (ctx.print() != null) return visit(ctx.print());

        throw new RuntimeException("Unknown statement");
    }

    @Override
    public Stmt visitAssignmentStatement(Information_flowParser.AssignmentStatementContext ctx) {
        if (ctx.expression() == null) {
            throw new RuntimeException("Invalid assignment statement: missing right-hand expression for " + ctx.IDENTIFIER().getText());
        }

        return setLocation(new AssignStmt(
                ctx.IDENTIFIER().getText(),
                (Expr) visit(ctx.expression())
        ), ctx);
    }

    @Override
    public Stmt visitSendStatement(Information_flowParser.SendStatementContext ctx) {
        return setLocation(new SendStmt(ctx.IDENTIFIER().getText()), ctx);
    }

    @Override
    public Node visitReceiveStatement(Information_flowParser.ReceiveStatementContext ctx) {

        Format pattern = buildReceivePattern((Expr) visit(ctx.expression()));
        CmdBlock cmdBlock = (CmdBlock) visit(ctx.cmdBlock());

        return setLocation(new TryReceiveStmt(pattern, cmdBlock), ctx);
    }

    @Override
    public Node visitReturnStatement(Information_flowParser.ReturnStatementContext ctx) {
        return setLocation(new ReturnStmt((Expr) visit(ctx.expression())), ctx);
    }

    @Override
    public Node visitPrint(Information_flowParser.PrintContext ctx) {

        List<Expr> args = new ArrayList<>();

        for (Information_flowParser.ExpressionContext e : ctx.expression()) {
            args.add((Expr) visit(e));
        }

        return setLocation(new PrintStmt(args), ctx);
    }

    // =========================
    // IF / WHILE
    // =========================

    @Override
    public Stmt visitIfStatement(Information_flowParser.IfStatementContext ctx) {

        IfStmt stmt = new IfStmt();

        stmt.condition = (Expr) visit(ctx.expression());
        stmt.thenCmdBlock = (CmdBlock) visit(ctx.cmdBlock());

        stmt.elseIfs = new ArrayList<>();

        for (Information_flowParser.ElseifStatementContext eif : ctx.elseifStatement()) {
            ElseIf ei = new ElseIf();
            ei.condition = (Expr) visit(eif.expression());
            ei.cmdBlock = (CmdBlock) visit(eif.cmdBlock());
            stmt.elseIfs.add(ei);
        }

        if (ctx.elseStatement() != null) {
            stmt.elseCmdBlock =
                    (CmdBlock) visit(ctx.elseStatement().cmdBlock());
        }

        return setLocation(stmt, ctx);
    }

    @Override
    public Stmt visitWhileStatement(Information_flowParser.WhileStatementContext ctx) {

        return setLocation(new WhileStmt(
                (Expr) visit(ctx.expression()),
                (CmdBlock) visit(ctx.cmdBlock())
        ), ctx);
    }

    // =========================
    // EXPRESSIONS
    // =========================

    @Override
    public Expr visitLogicalOr(Information_flowParser.LogicalOrContext ctx) {
        if (ctx.logicalOr() != null) {
            return new OpExpr(
                    (Expr) visit(ctx.logicalOr()),
                    "||",
                    (Expr) visit(ctx.logicalAnd())
            );
        }
        return (Expr) visit(ctx.logicalAnd());
    }

    @Override
    public Expr visitLogicalAnd(Information_flowParser.LogicalAndContext ctx) {
        if (ctx.logicalAnd() != null) {
            return new OpExpr(
                    (Expr) visit(ctx.logicalAnd()),
                    "&&",
                    (Expr) visit(ctx.equality())
            );
        }
        return (Expr) visit(ctx.equality());
    }

    @Override
    public Expr visitEquality(Information_flowParser.EqualityContext ctx) {
        if (ctx.equality() != null) {
            return new OpExpr(
                    (Expr) visit(ctx.equality()),
                    ctx.getChild(1).getText(),
                    (Expr) visit(ctx.relational())
            );
        }
        return (Expr) visit(ctx.relational());
    }

    @Override
    public Expr visitRelational(Information_flowParser.RelationalContext ctx) {
        if (ctx.relational() != null) {
            return new OpExpr(
                    (Expr) visit(ctx.relational()),
                    ctx.getChild(1).getText(),
                    (Expr) visit(ctx.additive())
            );
        }
        return (Expr) visit(ctx.additive());
    }

    @Override
    public Expr visitAdditive(Information_flowParser.AdditiveContext ctx) {

        if (ctx.additive() != null) {
            Expr left = (Expr) visit(ctx.additive());
            Expr right = (Expr) visit(ctx.multiplicative());

            String op = ctx.getChild(1).getText();

            return new OpExpr(left, op, right);
        }

        return (Expr) visit(ctx.multiplicative());
    }

    @Override
    public Expr visitMultiplicative(Information_flowParser.MultiplicativeContext ctx) {
        if (ctx.multiplicative() != null) {
            return new OpExpr(
                    (Expr) visit(ctx.multiplicative()),
                    ctx.getChild(1).getText(),
                    (Expr) visit(ctx.unary())
            );
        }
        return (Expr) visit(ctx.unary());
    }

    @Override
    public Expr visitUnary(Information_flowParser.UnaryContext ctx) {
        if (ctx.unary() != null) {
            return new UnaryExpr(
                    ctx.getChild(0).getText(),
                    (Expr) visit(ctx.unary())
            );
        }
        return (Expr) visit(ctx.primary());
    }

    @Override
    public Expr visitPrimary(Information_flowParser.PrimaryContext ctx) {

        if (ctx.INT() != null)
            return new IntLiteral(Integer.parseInt(ctx.INT().getText()));

        if (ctx.BOOL() != null)
            return new BoolLiteral(Boolean.parseBoolean(ctx.BOOL().getText()));

        if (ctx.STR() != null)
            return new StringLiteral(stripQuotes(ctx.STR().getText()));

        if (ctx.ENCRYPT() != null) {
            Expr key = buildKeyExpr(ctx.KEY());
            return setLocation(new EncryptExpr((Expr) visit(ctx.expression()), key), ctx);
        }

        if (ctx.IDENTIFIER() != null)
            return new VarExpr(ctx.IDENTIFIER().getText());

        if (ctx.methodCallOrFormat() != null)
            return (Expr) visit(ctx.methodCallOrFormat());

        return (Expr) visit(ctx.expression());
    }

    @Override
    public Expr visitMethodCallOrFormat(Information_flowParser.MethodCallOrFormatContext ctx) {

        String name =
                ctx.IDENTIFIER().getText();

        List<Expr> args =
                new ArrayList<>();

        if (ctx.argumentList() != null) {
            for (var exprCtx :
                    ctx.argumentList().expression()) {

                args.add(
                        (Expr) visit(exprCtx)
                );
            }
        }

        // FORMAT CONSTRUCTOR
        if (declaredFormats.contains(name)) {

            return new ConstructorExpr(
                    name,
                    args
            );
        }

        // NORMAL METHOD CALL
        MethodCallExpr call =
                new MethodCallExpr();

        call.name = name;
        call.args = args;

        return call;
    }

    private Operators parseType(Information_flowParser.TypeContext ctx) {
        if (ctx.encryptionType() != null) {
            return parseEncryptionType(ctx.encryptionType());
        }

        if (ctx.IDENTIFIER() != null) {
            return requireDeclaredFormatType(ctx.IDENTIFIER().getText(), ctx);
        }

        return parseBasicType(ctx.basicType().getText());
    }

    private Type parseBasicType(String t) {
        return switch (t) {
            case "int" -> Type.INT;
            case "bool" -> Type.BOOL;
            case "String" -> Type.STRING;
            default -> throw new RuntimeException("Unknown type: " + t);
        };
    }

    private CiphertextType parseEncryptionType(Information_flowParser.EncryptionTypeContext ctx) {
        String keyName = normalizeKey(ctx.KEY().getText());
        requireDeclaredKey(keyName, ctx);

        if (ctx.IDENTIFIER() != null) {
            String formatName = ctx.IDENTIFIER().getText();
            requireDeclaredFormat(formatName, ctx);
            return new CiphertextType(keyName, formatName);
        }

        throw new RuntimeException("Invalid encryption type");
    }

    private SecLabel parseSecLabel(String text) {
        return switch (text.toLowerCase()) {
            case "low" -> SecLabel.LOW;
            case "high" -> SecLabel.HIGH;
            default -> throw new RuntimeException("Unknown security label: " + text);
        };
    }

    private Privacy parsePrivacy(String p) {
        return p.equals("public") ? Privacy.PUBLIC : Privacy.PRIVATE;
    }

    private String stripQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }

    private Expr buildKeyExpr(TerminalNode keyNode) {
        if (keyNode == null) {
            throw new RuntimeException("Encryption requires a KEY token");
        }

        String keyName = normalizeKey(keyNode.getText());
        requireDeclaredKey(keyName, keyNode.getSymbol().getLine());
        return new StringLiteral(keyName);
    }

    private String normalizeKey(String text) {
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return stripQuotes(text);
        }

        return text;
    }

    /**
     * Receive syntax is parsed as an expression and converted into a pattern only
     * at the try_rcv boundary. This keeps constructors and encryption reusable in
     * ordinary expressions while preserving the existing matching implementation.
     */
    private Format buildReceivePattern(Expr expr) {
        if (expr instanceof EncryptExpr encryptExpr) {
            return new EncryptFormat(encryptExpr.keyExpr, buildReceivePattern(encryptExpr.payload));
        }

        if (expr instanceof ConstructorExpr constructorExpr) {
            requireDeclaredFormat(constructorExpr.name, expr.lineNumber);
            FormatType formatType = declaredFormatTypes.get(constructorExpr.name);
            if (constructorExpr.args.size() != formatType.fields.size()) {
                throw new TypeCheckException("Wrong number of fields in format pattern " + constructorExpr.name);
            }

            List<Format> args = new ArrayList<>();
            for (int i = 0; i < constructorExpr.args.size(); i++) {
                args.add(buildReceiveFieldPattern(constructorExpr.args.get(i), formatType.fields.get(i)));
            }
            return new ConstructorFormat(constructorExpr.name, args);
        }

        throw new RuntimeException("Receive pattern must be an encrypted value or format constructor");
    }

    private Format buildReceiveFieldPattern(Expr expr, Param expected) {
        if (expr instanceof TypedVarExpr typedVarExpr) {
            return new TypedVarFormat(
                    typedVarExpr.name,
                    typedVarExpr.assertedType,
                    typedVarExpr.assertedLabel
            );
        }

        if (expr instanceof VarExpr varExpr) {
            return new TypedVarFormat(
                    varExpr.name,
                    expected.type,
                    expected.label
            );
        }

        if (expr instanceof ConstructorExpr || expr instanceof EncryptExpr) {
            return buildReceivePattern(expr);
        }

        // Arithmetic and ordinary variable expressions are value checks rather
        // than new bindings in a receive pattern.
        return new ExprFormat(expr);
    }

    private List<Param> buildParams(Information_flowParser.DeclsContext ctx) {

        List<Param> params = new ArrayList<>();

        for (var item : ctx.declItem()) {

            Param p = new Param();
            p.type = parseType(item.type());
            p.label = parseSecLabel(item.SECLABEL().getText());
            p.name = item.IDENTIFIER().getText();

            params.add(p);
        }

        return params;
    }

    private <T extends Node> T setLocation(T node, ParserRuleContext ctx) {
        if (node != null && ctx != null) {
            node.lineNumber = ctx.getStart().getLine();
        }
        return node;
    }

    private void declareKey(Information_flowParser.KeyDeclarationContext ctx) {
        String keyName = normalizeKey(ctx.KEY().getText());
        if (!declaredKeys.add(keyName)) {
            throw new RuntimeException("Duplicate global key declaration at line " + ctx.getStart().getLine() + ": " + keyName);
        }
    }

    private void declareFormat(Information_flowParser.FormatDeclarationContext ctx) {
        String formatName = ctx.IDENTIFIER().getText();
        if (!declaredFormats.add(formatName)) {
            throw new RuntimeException("Duplicate global format declaration at line " + ctx.getStart().getLine() + ": " + formatName);
        }

        List<Param> fields = ctx.decls() == null ? List.of() : buildParams(ctx.decls());
        declaredFormatTypes.put(formatName, new FormatType(formatName, fields));
    }

    private void requireDeclaredKey(String keyName, ParserRuleContext ctx) {
        requireDeclaredKey(keyName, ctx.getStart().getLine());
    }

    private void requireDeclaredKey(String keyName, int lineNumber) {
        if (!declaredKeys.contains(keyName)) {
            throw new RuntimeException("Undeclared global key at line " + lineNumber + ": " + keyName);
        }
    }

    private void requireDeclaredFormat(String formatName, ParserRuleContext ctx) {
        requireDeclaredFormat(formatName, ctx.getStart().getLine());
    }

    private void requireDeclaredFormat(String formatName, int lineNumber) {
        if (!declaredFormats.contains(formatName)) {
            throw new RuntimeException("Undeclared global format at line " + lineNumber + ": " + formatName);
        }
    }

    private FormatType requireDeclaredFormatType(String formatName, ParserRuleContext ctx) {
        requireDeclaredFormat(formatName, ctx);
        return declaredFormatTypes.get(formatName);
    }

}
