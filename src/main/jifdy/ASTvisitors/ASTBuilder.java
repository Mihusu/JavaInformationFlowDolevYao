package ASTvisitors;

import ASTnodes.*;
import antlr.Information_flowBaseVisitor;
import antlr.Information_flowParser;
import ASTnodes.Expr.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * ANTLR visitor that lowers parse-tree nodes into the project's AST representation.
 */
public class ASTBuilder extends Information_flowBaseVisitor<Node> {

    // =========================
    // PROGRAM / CLASS
    // =========================

    @Override
    public Node visitProgram(Information_flowParser.ProgramContext ctx) {
        ClassDecl cls = (ClassDecl) visit(ctx.class_());
        return setLocation(new Program(List.of(cls)), ctx);
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

        for (Information_flowParser.DeclarationContext d : ctx.declaration()) {
            cls.declarations.add((Declaration) visit(d));
        }

        if (ctx.functionDeclaration() != null) {
            for (Information_flowParser.FunctionDeclarationContext fctx : ctx.functionDeclaration()) {
                cls.functions.add((FunctionDecl) visit(fctx));
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

            if (ctx.type() != null) {
                decl.type = parseType(ctx.type());
            } else if (ctx.encryptionType() != null) {
                decl.type = Type.CIPHERTEXT;
            } else {
                decl.type = Type.CIPHERTEXT; // fallback
            }

            // Safe SECLABEL retrieval
            if (ctx.SECLABEL() != null) {
                decl.label = parseSecLabel(ctx.SECLABEL().getText());
            } else {
                // Should not happen with current grammar, but for safety:
                decl.label = SecLabel.LOW;
            }

            decl.name = ctx.IDENTIFIER().getText();

            if (isEncryptedDeclaration(ctx)) {
                Expr key = buildDeclarationKeyExpr(ctx);
                Expr payload = ctx.format() == null
                        ? (Expr) visit(ctx.expression())
                        : buildPayloadExpr(ctx.format());

                decl.type = Type.CIPHERTEXT;
                decl.init = setLocation(new EncryptExpr(payload, key), ctx);
            } else if (ctx.expression() != null) {
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

        String resultVar = ctx.IDENTIFIER() != null ? ctx.IDENTIFIER().getText() : null;

        return setLocation(new TryReceiveStmt(pattern, resultVar, cmdBlock), ctx);
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

        if (ctx.IDENTIFIER() != null)
            return new VarExpr(ctx.IDENTIFIER().getText());

        if (ctx.ENCRYPT() != null) {
            Expr key = buildKeyExpr(ctx.getToken(Information_flowParser.KEY, 0));
            Expr payload = ctx.format() == null
                    ? (Expr) visit(ctx.expression())
                    : buildPayloadExpr(ctx.format());

            return new EncryptExpr(
                    payload,
                    key
            );
        }

        if (ctx.functionCall() != null)
            return (Expr) visit(ctx.functionCall());

        return (Expr) visit(ctx.expression());
    }

    @Override
    public Expr visitFunctionCall(Information_flowParser.FunctionCallContext ctx) {

        FunctionCallExpr call = new FunctionCallExpr();
        call.name = ctx.IDENTIFIER().getText();
        call.args = new ArrayList<>();

        if (ctx.argumentList() != null) {
            for (var exprCtx : ctx.argumentList().expression()) {
                call.args.add((Expr) visit(exprCtx));
            }
        }

        return call;
    }

    // =========================
    // PATTERNS (DOLEV-YAO)
    // =========================

    @Override
    public Format visitFormat(Information_flowParser.FormatContext ctx) {

        // X
        if (ctx.IDENTIFIER() != null && ctx.formatList() == null)
            return new TypedVarPattern(
                    ctx.IDENTIFIER().getText(),
                    ctx.type() == null ? null : parseType(ctx.type()),
                    parseSecLabel(ctx.SECLABEL().getText())
            );

        // f(X,Y)
        if (ctx.formatList() != null) {

            List<Format> args = new ArrayList<>();

            for (var p : ctx.formatList().format()) {
                args.add((Format) visit(p));
            }

            return new ConstructorPattern(
                    ctx.IDENTIFIER().getText(),
                    args
            );
        }

        // encrypt(keyExpr, pattern)
        if (ctx.ENCRYPT() != null) {

            Expr key = buildFormatKeyExpr(ctx);

            Format inner =
                    (Format) visit(ctx.format());

            return new EncryptPattern(key, inner);
        }

        throw new RuntimeException("Invalid format");
    }


    private Type parseType(Information_flowParser.TypeContext ctx) {
        if (ctx.encryptionType() != null) {
            return Type.CIPHERTEXT;
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
                && (ctx.expression() != null || ctx.format() != null);
    }

    private Expr buildDeclarationKeyExpr(Information_flowParser.DeclarationContext ctx) {
        TerminalNode keyNode = ctx.getToken(Information_flowParser.KEY, 0);
        if (keyNode != null) {
            return buildKeyExpr(keyNode);
        }

        throw new RuntimeException("Encryption declaration requires a key");
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

        return new StringLiteral(normalizeKey(keyNode.getText()));
    }

    private String normalizeKey(String text) {
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return stripQuotes(text);
        }

        return text;
    }

    private Expr buildPayloadExpr(Information_flowParser.FormatContext ctx) {
        if (ctx.IDENTIFIER() != null && ctx.formatList() == null) {
            return new VarExpr(ctx.IDENTIFIER().getText());
        }

        if (ctx.formatList() != null) {
            List<Expr> args = new ArrayList<>();

            for (var p : ctx.formatList().format()) {
                args.add(buildPayloadExpr(p));
            }

            return new ConstructorExpr(ctx.IDENTIFIER().getText(), args);
        }

        if (ctx.ENCRYPT() != null) {
            return new EncryptExpr(
                    buildPayloadExpr(ctx.format()),
                    buildFormatKeyExpr(ctx)
            );
        }

        throw new RuntimeException("Invalid encrypted payload expression");
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

}
