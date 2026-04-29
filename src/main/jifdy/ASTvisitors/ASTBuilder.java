package ASTvisitors;

import ASTnodes.*;
import antlr.Information_flowBaseVisitor;
import antlr.Information_flowParser;
import ASTnodes.Expr.*;

import java.util.ArrayList;
import java.util.List;

public class ASTBuilder extends Information_flowBaseVisitor<Node> {

    // =========================
    // PROGRAM / CLASS
    // =========================

    @Override
    public Node visitProgram(Information_flowParser.ProgramContext ctx) {
        ClassDecl cls = (ClassDecl) visit(ctx.class_());
        return new Program(List.of(cls));
    }

    @Override
    public Node visitClass(Information_flowParser.ClassContext ctx) {

        Privacy privacy = null;
        if (ctx.PPLABEL() != null) {
            privacy = parsePrivacy(ctx.PPLABEL().getText());
        }

        ClassDecl cls = new ClassDecl();
        cls.privacy = privacy;

        return visit(ctx.classBlock());
    }

    @Override
    public Node visitClassBlock(Information_flowParser.ClassBlockContext ctx) {

        ClassDecl cls = new ClassDecl();
        cls.name = ctx.IDENTIFIER().getText();

        cls.declarations = new ArrayList<>();
        cls.functions = new ArrayList<>();

        for (var d : ctx.declaration()) {
            cls.declarations.add((Declaration) visit(d));
        }

        if (ctx.functionDeclaration() != null) {
            for (var fctx : ctx.functionDeclaration()) {
                cls.functions.add((FunctionDecl) visit(fctx));
            }
        }

        return cls;
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
            f.returnType = parseType(ctx.type().getText());
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
            for (var dctx : ctx.decls()) {
                f.params.addAll(buildParams(dctx));
            }
        }

        // body
        f.body = (CmdBlock) visit(ctx.cmdBlock());

        return f;
    }

    @Override
    public Node visitDeclaration(Information_flowParser.DeclarationContext ctx) {

        // Variable declaration
        if (ctx.type() != null) {

            VarDecl decl = new VarDecl();

            decl.type = parseType(ctx.type().getText());
            decl.label = parseSecLabel(ctx.SECLABEL().getText());
            decl.name = ctx.IDENTIFIER().getText();

            if (ctx.expression() != null) {
                decl.init = (Expr) visit(ctx.expression());
            }

            return decl;
        }

        // Procedure declaration
        ProcDecl proc = new ProcDecl();
        proc.privacy = parsePrivacy(ctx.PPLABEL().getText());
        proc.name = ctx.IDENTIFIER().getText();

        if (ctx.decls() != null)
            proc.params = buildParams((Information_flowParser.DeclsContext) ctx.decls());
        else
            proc.params = new ArrayList<>();
        proc.body = new ArrayList<>();

        for (var stmt : ctx.assignmentStatement()) {
            proc.body.add((Stmt) visit(stmt));
        }

        return proc;
    }

    // =========================
    // BLOCK
    // =========================

    @Override
    public CmdBlock visitCmdBlock(Information_flowParser.CmdBlockContext ctx) {

        CmdBlock block = new CmdBlock();
        block.statements = new ArrayList<>();

        for (var d : ctx.declaration()) {
            block.statements.add(visit(d));
        }

        for (var s : ctx.statement()) {
            block.statements.add(visit(s));
        }

        for (var b : ctx.cmdBlock()) {
            block.statements.add(visit(b));
        }

        return block;
    }

    // =========================
    // STATEMENTS
    // =========================

    @Override
    public Stmt visitAssignmentStatement(Information_flowParser.AssignmentStatementContext ctx) {

        return new AssignStmt(
                ctx.IDENTIFIER().getText(),
                (Expr) visit(ctx.expression())
        );
    }

    @Override
    public Node visitEncryptStatement(
            Information_flowParser.EncryptStatementContext ctx) {

        String target = ctx.IDENTIFIER().getText();

        Expr payload = (Expr) visit(ctx.expression(0));
        Expr key     = (Expr) visit(ctx.expression(1));

        return new EncryptStmt(target, payload, key);
    }

    @Override
    public Stmt visitSendStatement(Information_flowParser.SendStatementContext ctx) {
        return new SendStmt(ctx.IDENTIFIER().getText());
    }

    @Override
    public Node visitReceiveStatement(Information_flowParser.ReceiveStatementContext ctx) {

        ReceivePattern pattern = (ReceivePattern) visit(ctx.receivePattern());
        CmdBlock cmdBlock = (CmdBlock) visit(ctx.cmdBlock());

        return new TryReceiveStmt(pattern, cmdBlock);
    }

    @Override
    public Node visitReturnStatement(Information_flowParser.ReturnStatementContext ctx) {
        return new ReturnStmt((Expr) visit(ctx.expression()));
    }

    @Override
    public Node visitPrint(Information_flowParser.PrintContext ctx) {

        List<Expr> args = new ArrayList<>();

        for (var e : ctx.expression()) {
            args.add((Expr) visit(e));
        }

        return new PrintStmt(args);
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

        for (var eif : ctx.elseifStatement()) {
            ElseIf ei = new ElseIf();
            ei.condition = (Expr) visit(eif.expression());
            ei.cmdBlock = (CmdBlock) visit(eif.cmdBlock());
            stmt.elseIfs.add(ei);
        }

        if (ctx.elseStatement() != null) {
            stmt.elseCmdBlock =
                    (CmdBlock) visit(ctx.elseStatement().cmdBlock());
        }

        return stmt;
    }

    @Override
    public Stmt visitWhileStatement(Information_flowParser.WhileStatementContext ctx) {

        return new WhileStmt(
                (Expr) visit(ctx.expression()),
                (CmdBlock) visit(ctx.cmdBlock())
        );
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
    public Expr visitPrimary(Information_flowParser.PrimaryContext ctx) {

        if (ctx.INT() != null)
            return new IntLiteral(Integer.parseInt(ctx.INT().getText()));

        if (ctx.BOOL() != null)
            return new BoolLiteral(Boolean.parseBoolean(ctx.BOOL().getText()));

        if (ctx.STR() != null)
            return new StringLiteral(stripQuotes(ctx.STR().getText()));

        if (ctx.IDENTIFIER() != null)
            return new VarExpr(ctx.IDENTIFIER().getText());

        if (ctx.functionCall() != null)
            return (Expr) visit(ctx.functionCall());

        return (Expr) visit(ctx.expression());
    }

    @Override
    public Expr visitFunctionCall(Information_flowParser.FunctionCallContext ctx) {

        FunctionCall call = new FunctionCall();
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
    public ReceivePattern visitReceivePattern(
            Information_flowParser.ReceivePatternContext ctx) {

        // X
        if (ctx.IDENTIFIER() != null && ctx.receivePatternList() == null)
            return new VarPattern(ctx.IDENTIFIER().getText());

        // f(X,Y)
        if (ctx.receivePatternList() != null) {

            List<ReceivePattern> args = new ArrayList<>();

            for (var p : ctx.receivePatternList().receivePattern()) {
                args.add((ReceivePattern) visit(p));
            }

            return new ConstructorPattern(
                    ctx.IDENTIFIER().getText(),
                    args
            );
        }

        // encrypt(keyExpr, pattern)
        if (ctx.ENCRYPT() != null) {

            Expr key =
                    (Expr) visit(ctx.expression());

            ReceivePattern inner =
                    (ReceivePattern) visit(ctx.receivePattern());

            return new EncryptPattern(key, inner);
        }

        throw new RuntimeException("Invalid receive pattern");
    }


    private Type parseType(String t) {
        return switch (t) {
            case "int" -> Type.INT;
            case "bool" -> Type.BOOL;
            case "String" -> Type.STRING;
            case "ciphertext" -> Type.CIPHERTEXT;
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

    private List<Param> buildParams(Information_flowParser.DeclsContext ctx) {

        List<Param> params = new ArrayList<>();

        for (var item : ctx.declItem()) {

            Param p = new Param();
            p.type = parseType(item.type().getText());
            p.label = parseSecLabel(item.SECLABEL().getText());
            p.name = item.IDENTIFIER().getText();

            params.add(p);
        }

        return params;
    }
}
