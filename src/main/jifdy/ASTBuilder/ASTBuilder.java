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
        cls.functions = new ArrayList<>();
        cls.entryStatements = new ArrayList<>();

        for (Information_flowParser.DeclarationContext d : ctx.declaration()) {
            cls.declarations.add((Declaration) visit(d));
        }

        if (ctx.functionDeclaration() != null) {
            for (Information_flowParser.FunctionDeclarationContext fctx : ctx.functionDeclaration()) {
                cls.functions.add((FunctionDecl) visit(fctx));
            }
        }

        if (ctx.statement() != null) {
            for (Information_flowParser.StatementContext sctx : ctx.statement()) {
                cls.entryStatements.add((Stmt) visit(sctx));
            }
        }

        return setLocation(cls, ctx);
    }

    // =========================
    // DECLARATIONS
    // =========================

    @Override
    public Node visitFunctionDeclaration(Information_flowParser.FunctionDeclarationContext ctx) {

        FunctionDecl f = new FunctionDecl();

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

            // -------------------------
            // Variable name + declared type
            // -------------------------
            // Grammar alternatives:
            // 1) type SECLABEL IDENTIFIER ('=' expression)? ';'
            // 2) encryptionType SECLABEL IDENTIFIER '=' e(KEY, (expression|format)) ';'
            // 3) IDENTIFIER SECLABEL IDENTIFIER '=' format ';'   // IDENTIFIER is a declared format type
            if (ctx.type() != null) {
                decl.type = parseType(ctx.type());
                decl.name = ctx.IDENTIFIER(0).getText();
            } else if (ctx.encryptionType() != null) {
                decl.type = parseEncryptionType(ctx.encryptionType());
                decl.name = ctx.IDENTIFIER(0).getText();
            } else if (ctx.format() != null && ctx.IDENTIFIER().size() >= 2) {
                // Format-typed variable declaration: <FormatName> <sec> <var> = <formatLiteral>;
                decl.type = requireDeclaredFormatType(ctx.IDENTIFIER(0).getText(), ctx);
                decl.name = ctx.IDENTIFIER(1).getText();
            } else {
                throw new RuntimeException("Invalid declaration at line " + ctx.getStart().getLine());
            }

            // Variables always have an explicit security label in the grammar.
            decl.label = parseSecLabel(ctx.SECLABEL().getText());

            if (isEncryptedDeclaration(ctx)) {
                Expr key = buildDeclarationKeyExpr(ctx);
                Expr payload = buildEncryptedDeclarationPayload(ctx);

                decl.type = parseEncryptionType(ctx.encryptionType());
                decl.init = setLocation(new EncryptExpr(payload, key), ctx);
            } else if (ctx.expression() != null) {
                decl.init = (Expr) visit(ctx.expression());
            } else if (ctx.format() != null) {
                validateFormatLiteral(ctx.format());
                decl.init = buildPayloadExpr(ctx.format());
            }

            return setLocation(decl, ctx);
        }

        // Procedure declaration
        ProcDecl proc = new ProcDecl();
        proc.privacy = parsePrivacy(ctx.PPLABEL().getText());
        proc.name = ctx.IDENTIFIER(0).getText();

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
        if (ctx.functionCall() != null) return new FunctionCallStmt((Expr) visit(ctx.functionCall()));
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

        Format pattern = (Format) visit(ctx.format());
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

        if (ctx.typedRef() != null)
            return (Expr) visit(ctx.typedRef());

        if (ctx.IDENTIFIER() != null)
            return new VarExpr(ctx.IDENTIFIER().getText());

        if (ctx.functionCall() != null)
            return (Expr) visit(ctx.functionCall());

        return (Expr) visit(ctx.expression());
    }

    @Override
    public Expr visitTypedRef(Information_flowParser.TypedRefContext ctx) {
        return setLocation(
                new TypedVarExpr(
                        ctx.IDENTIFIER().getText(),
                        parseType(ctx.type()),
                        parseSecLabel(ctx.SECLABEL().getText())
                ),
                ctx
        );
    }

    @Override
    public Expr visitFunctionCall(Information_flowParser.FunctionCallContext ctx) {

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

        // NORMAL FUNCTION CALL
        FunctionCallExpr call =
                new FunctionCallExpr();

        call.name = name;
        call.args = args;

        return call;
    }

    // =========================
    // PATTERNS (DOLEV-YAO)
    // =========================

    @Override
    public Format visitFormat(Information_flowParser.FormatContext ctx) {

        // Expression-shaped format field, e.g. `int high amount1 - int high amount2`.
        // On the receive side this becomes a value-checking ExprFormat; on the send side
        // buildPayloadExpr applies the same lowering into an Expr payload.
        if (isFormatBinaryExpr(ctx)) {
            return new ExprFormat(
                    new OpExpr(
                            buildPayloadExpr(ctx.format(0)),
                            ctx.getChild(1).getText(),
                            buildPayloadExpr(ctx.format(1))
                    )
            );
        }

        // Unary minus in a format field is also an expression, not a new binding.
        if (isFormatUnaryExpr(ctx)) {
            return new ExprFormat(
                    new UnaryExpr(
                            ctx.getChild(0).getText(),
                            buildPayloadExpr(ctx.format(0))
                    )
            );
        }

        // X / int high X: a pattern binding on receive, or a typed variable reference
        // when buildPayloadExpr handles constructor payload syntax.
        if (ctx.IDENTIFIER() != null && ctx.formatList() == null && !isConstructorFormat(ctx))
            return new TypedVarFormat(
                    ctx.IDENTIFIER().getText(),
                    ctx.type() == null ? null : parseType(ctx.type()),
                    parseSecLabel(ctx.SECLABEL().getText())
            );

        // f(X,Y)
        if (isConstructorFormat(ctx)) {
            requireDeclaredFormat(ctx.IDENTIFIER().getText(), ctx);
            validateFormatLiteral(ctx);

            List<Format> args = new ArrayList<>();

            if (ctx.formatList() != null) {
                for (var p : ctx.formatList().format()) {
                    args.add((Format) visit(p));
                }
            }

            return new ConstructorFormat(
                    ctx.IDENTIFIER().getText(),
                    args
            );
        }

        // encrypt(keyExpr, pattern)
        if (ctx.ENCRYPT() != null) {

            Expr key = buildFormatKeyExpr(ctx);

            Format inner =
                    (Format) visit(ctx.format(0));

            return new EncryptFormat(key, inner);
        }

        throw new RuntimeException("Invalid format");
    }


    private Operators parseType(Information_flowParser.TypeContext ctx) {
        if (ctx.encryptionType() != null) {
            return parseEncryptionType(ctx.encryptionType());
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

    private boolean isEncryptedDeclaration(Information_flowParser.DeclarationContext ctx) {
        return ctx.ENCRYPT() != null
                && ctx.getToken(Information_flowParser.KEY, 0) != null
                && (ctx.expression() != null || ctx.format() != null || ctx.IDENTIFIER().size() >= 2);
    }

    private Expr buildDeclarationKeyExpr(Information_flowParser.DeclarationContext ctx) {
        TerminalNode keyNode = ctx.getToken(Information_flowParser.KEY, 0);
        if (keyNode != null) {
            return buildKeyExpr(keyNode);
        }

        throw new RuntimeException("Encryption declaration requires a key");
    }

    private Expr buildEncryptedDeclarationPayload(Information_flowParser.DeclarationContext ctx) {
        if (ctx.format() != null) {
            return buildPayloadExpr(ctx.format());
        }

        if (ctx.expression() != null) {
            return (Expr) visit(ctx.expression());
        }

        // In `e(k, u)`, the payload is parsed by the encrypted-declaration
        // alternative as the second direct IDENTIFIER. The first one is the
        // declared ciphertext variable name.
        if (ctx.IDENTIFIER().size() >= 2) {
            return setLocation(new VarExpr(ctx.IDENTIFIER(1).getText()), ctx);
        }

        throw new RuntimeException("Encryption declaration requires a payload");
    }

    private Expr buildFormatKeyExpr(Information_flowParser.FormatContext ctx) {
        TerminalNode keyNode = ctx.getToken(Information_flowParser.KEY, 0);
        if (keyNode != null) {
            return buildKeyExpr(keyNode);
        }

        throw new RuntimeException("Encryption pattern requires a key");
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

    private Expr buildPayloadExpr(Information_flowParser.FormatContext ctx) {
        // Constructor payloads reuse the `format` grammar so users can write
        // Transfer(int high a - int high b). Convert those format-shaped terms
        // into ordinary expression AST nodes before evaluation/code generation.
        if (isFormatBinaryExpr(ctx)) {
            return setLocation(
                    new OpExpr(
                            buildPayloadExpr(ctx.format(0)),
                            ctx.getChild(1).getText(),
                            buildPayloadExpr(ctx.format(1))
                    ),
                    ctx
            );
        }

        // Same conversion for unary minus, e.g. Transfer(- int high amount).
        if (isFormatUnaryExpr(ctx)) {
            return setLocation(
                    new UnaryExpr(
                            ctx.getChild(0).getText(),
                            buildPayloadExpr(ctx.format(0))
                    ),
                    ctx
            );
        }

        if (ctx.IDENTIFIER() != null && ctx.formatList() == null && !isConstructorFormat(ctx)) {
            // A typed field in payload position is not a declaration; it asserts the
            // existing variable's type/label and then compiles/evaluates as that variable.
            Expr var = ctx.type() == null
                    ? new VarExpr(ctx.IDENTIFIER().getText())
                    : new TypedVarExpr(
                            ctx.IDENTIFIER().getText(),
                            parseType(ctx.type()),
                            parseSecLabel(ctx.SECLABEL().getText())
                    );
            return setLocation(var, ctx);
        }

        if (isConstructorFormat(ctx)) {
            requireDeclaredFormat(ctx.IDENTIFIER().getText(), ctx);
            validateFormatLiteral(ctx);
            List<Expr> args = new ArrayList<>();

            if (ctx.formatList() != null) {
                for (var p : ctx.formatList().format()) {
                    args.add(buildPayloadExpr(p));
                }
            }

            return new ConstructorExpr(ctx.IDENTIFIER().getText(), args);
        }

        if (ctx.ENCRYPT() != null) {
            return new EncryptExpr(
                    buildPayloadExpr(ctx.format(0)),
                    buildFormatKeyExpr(ctx)
            );
        }

        throw new RuntimeException("Invalid encrypted payload expression");
    }

    private boolean isFormatBinaryExpr(Information_flowParser.FormatContext ctx) {
        if (ctx.getChildCount() != 3) {
            return false;
        }

        // Keep this list aligned with the binary operators accepted in the `format` rule.
        return switch (ctx.getChild(1).getText()) {
            case "+", "-", "*", "/", "%" -> true;
            default -> false;
        };
    }

    private boolean isFormatUnaryExpr(Information_flowParser.FormatContext ctx) {
        return ctx.getChildCount() == 2 && "-".equals(ctx.getChild(0).getText());
    }

    private boolean isConstructorFormat(Information_flowParser.FormatContext ctx) {
        return ctx.IDENTIFIER() != null
                && ctx.getChildCount() >= 3
                && "(".equals(ctx.getChild(1).getText());
    }

    private String extractFormatName(Information_flowParser.FormatContext ctx, ParserRuleContext origin) {
        if (ctx.IDENTIFIER() != null) {
            String formatName = ctx.IDENTIFIER().getText();
            requireDeclaredFormat(formatName, origin);
            return formatName;
        }

        if (ctx.ENCRYPT() != null) {
            return extractFormatName(ctx.format(0), origin);
        }

        throw new RuntimeException("Invalid format name in ciphertext type");
    }

    private String extractExpressionFormatName(Expr expr, ParserRuleContext origin) {
        if (expr instanceof ConstructorExpr constructorExpr) {
            requireDeclaredFormat(constructorExpr.name, origin);
            return constructorExpr.name;
        }

        if (expr instanceof VarExpr varExpr) {
            requireDeclaredFormat(varExpr.name, origin);
            return varExpr.name;
        }

        if (expr instanceof Expr.StringLiteral literal) {
            requireDeclaredFormat(literal.value, origin);
            return literal.value;
        }

        if (expr instanceof FunctionCallExpr functionCallExpr) {
            requireDeclaredFormat(functionCallExpr.name, origin);
            return functionCallExpr.name;
        }

        if (expr instanceof EncryptExpr encryptExpr) {
            requireDeclaredFormat(encryptExpr.formatName, origin);
            return encryptExpr.formatName;
        }

        throw new RuntimeException("Cannot derive ciphertext format from expression");
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
        if (!declaredFormats.contains(formatName)) {
            throw new RuntimeException("Undeclared global format at line " + ctx.getStart().getLine() + ": " + formatName);
        }
    }

    private FormatType requireDeclaredFormatType(String formatName, ParserRuleContext ctx) {
        requireDeclaredFormat(formatName, ctx);
        return declaredFormatTypes.get(formatName);
    }

    private void validateFormatLiteral(Information_flowParser.FormatContext ctx) {
        if (ctx == null || !isConstructorFormat(ctx)) {
            return;
        }

        FormatType declaredFormat = requireDeclaredFormatType(ctx.IDENTIFIER().getText(), ctx);
        List<Information_flowParser.FormatContext> actualArgs = ctx.formatList() == null
                ? List.of()
                : ctx.formatList().format();

        if (declaredFormat.fields.size() != actualArgs.size()) {
            throw new TypeCheckException("Format arity mismatch at line " + ctx.getStart().getLine() + ": " + declaredFormat.name);
        }

        for (int i = 0; i < actualArgs.size(); i++) {
            Param expected = declaredFormat.fields.get(i);
            Information_flowParser.FormatContext actual = actualArgs.get(i);

            if (isFormatBinaryExpr(actual) || isFormatUnaryExpr(actual)) {
                continue;
            }

            if (isConstructorFormat(actual)) {
                FormatType actualFormatType = requireDeclaredFormatType(actual.IDENTIFIER().getText(), actual);
                if (!Operators.sameType(expected.type, actualFormatType)) {
                    throw new TypeCheckException("Format field type mismatch at line " + actual.getStart().getLine() + ": expected " + expected.type + " but got " + actualFormatType);
                }
                validateFormatLiteral(actual);
                continue;
            }

            if (actual.type() == null) {
                throw new TypeCheckException("Format field must declare type at line " + actual.getStart().getLine());
            }

            Operators actualType = parseType(actual.type());
            SecLabel actualLabel = parseSecLabel(actual.SECLABEL().getText());

            if (!Operators.sameType(expected.type, actualType) || expected.label != actualLabel) {
                throw new TypeCheckException(
                        "Format field mismatch at line " + actual.getStart().getLine() +
                                ": expected " + expected.type + " " + expected.label +
                                " but got " + actualType + " " + actualLabel
                );
            }
        }
    }
}
