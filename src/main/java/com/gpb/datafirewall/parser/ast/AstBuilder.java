package com.gpb.datafirewall.parser.ast;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.gpb.datafirewall.rules.parser.SqlWhereBaseVisitor;
import com.gpb.datafirewall.rules.parser.SqlWhereParser;

import java.util.*;

/**
 * AST builder, парсинг из SQL с помощью ANTLR
 */
public class AstBuilder extends SqlWhereBaseVisitor<Expr> {

    @Override
    public Expr visitParse(SqlWhereParser.ParseContext ctx) {
        return visit(ctx.expression());
    }

    // --------------------
    // boolean expressions
    // --------------------

    @Override
    public Expr visitAndExpr(SqlWhereParser.AndExprContext ctx) {
        return new AndExpr(visit(ctx.expression(0)), visit(ctx.expression(1)));
    }

    @Override
    public Expr visitOrExpr(SqlWhereParser.OrExprContext ctx) {
        return new OrExpr(visit(ctx.expression(0)), visit(ctx.expression(1)));
    }

    @Override
    public Expr visitNotExpr(SqlWhereParser.NotExprContext ctx) {
        return new NotExpr(visit(ctx.expression()));
    }

    @Override
    public Expr visitParenExpr(SqlWhereParser.ParenExprContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Expr visitPredicateOnly(SqlWhereParser.PredicateOnlyContext ctx) {
        return visit(ctx.predicate());
    }

    @Override
    public Expr visitIsNullExpr(SqlWhereParser.IsNullExprContext ctx) {
        return new IsNullExpr(visit(ctx.value()));
    }

    @Override
    public Expr visitIsNotNullExpr(SqlWhereParser.IsNotNullExprContext ctx) {
        return new IsNotNullExpr(visit(ctx.value()));
    }

    @Override
    public Expr visitCompareExpr(SqlWhereParser.CompareExprContext ctx) {
        Expr l = visit(ctx.value(0));
        Expr r = visit(ctx.value(1));
        String op = ctx.cmpOp().getText();
        return new CompareExpr(l, op, r);
    }

    @Override
    public Expr visitLikeExpr(SqlWhereParser.LikeExprContext ctx) {
        Expr val = visit(ctx.value());
        String tk = ctx.STRING().getText();
        String s = tk.substring(1, tk.length() - 1).replace("\\'", "'");
        return new LikeExpr(val, s);
    }

    @Override
    public Expr visitInExpr(SqlWhereParser.InExprContext ctx) {
        Expr val = visit(ctx.value(0));
        List<Expr> options = new ArrayList<>();
        for (int i = 1; i < ctx.value().size(); i++) {
            options.add(visit(ctx.value(i)));
        }
        return new InExpr(val, options);
    }

    // --------------------
    // value expressions (arith)
    // --------------------

    /**
     * value : additiveExpr # ValueExpr
     */
    @Override
    public Expr visitValueExpr(SqlWhereParser.ValueExprContext ctx) {
        return visit(ctx.additiveExpr());
    }

    /**
     * additiveExpr : additiveExpr PLUS multiplicativeExpr # AddExpr
     */
    @Override
    public Expr visitAddExpr(SqlWhereParser.AddExprContext ctx) {
        return new com.gpb.datafirewall.parser.ast.AddExpr(
                visit(ctx.additiveExpr()),
                visit(ctx.multiplicativeExpr())
        );
    }

    /**
     * additiveExpr : additiveExpr MINUS multiplicativeExpr # SubExpr
     */
    @Override
    public Expr visitSubExpr(SqlWhereParser.SubExprContext ctx) {
        return new com.gpb.datafirewall.parser.ast.SubExpr(
                visit(ctx.additiveExpr()),
                visit(ctx.multiplicativeExpr())
        );
    }

    /**
     * additiveExpr : multiplicativeExpr # ToMul
     */
    @Override
    public Expr visitToMul(SqlWhereParser.ToMulContext ctx) {
        return visit(ctx.multiplicativeExpr());
    }

    /**
     * multiplicativeExpr : multiplicativeExpr MUL unaryExpr # MulExpr
     */
    @Override
    public Expr visitMulExpr(SqlWhereParser.MulExprContext ctx) {
        return new com.gpb.datafirewall.parser.ast.MulExpr(
                visit(ctx.multiplicativeExpr()),
                visit(ctx.unaryExpr())
        );
    }

    /**
     * multiplicativeExpr : multiplicativeExpr DIV unaryExpr # DivExpr
     */
    @Override
    public Expr visitDivExpr(SqlWhereParser.DivExprContext ctx) {
        return new com.gpb.datafirewall.parser.ast.DivExpr(
                visit(ctx.multiplicativeExpr()),
                visit(ctx.unaryExpr())
        );
    }

    /**
     * multiplicativeExpr : multiplicativeExpr MOD unaryExpr # ModExpr
     */
    @Override
    public Expr visitModExpr(SqlWhereParser.ModExprContext ctx) {
        return new com.gpb.datafirewall.parser.ast.ModExpr(
                visit(ctx.multiplicativeExpr()),
                visit(ctx.unaryExpr())
        );
    }

    /**
     * multiplicativeExpr : unaryExpr # ToUnary
     */
    @Override
    public Expr visitToUnary(SqlWhereParser.ToUnaryContext ctx) {
        return visit(ctx.unaryExpr());
    }

    /**
     * unaryExpr : MINUS unaryExpr # UnaryMinusExpr
     */
    @Override
    public Expr visitUnaryMinusExpr(SqlWhereParser.UnaryMinusExprContext ctx) {
        return new com.gpb.datafirewall.parser.ast.UnaryMinusExpr(visit(ctx.unaryExpr()));
    }

    /**
     * unaryExpr : atom # ToAtom
     */
    @Override
    public Expr visitToAtom(SqlWhereParser.ToAtomContext ctx) {
        return visit(ctx.atom());
    }

    /**
     * atom : LPAREN value RPAREN # ValueParen
     */
    @Override
    public Expr visitValueParen(SqlWhereParser.ValueParenContext ctx) {
        return visit(ctx.value());
    }

    @Override
    public Expr visitFuncCall(SqlWhereParser.FuncCallContext ctx) {
        StringBuilder sb = new StringBuilder();
        List<TerminalNode> ids = ctx.IDENT();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(".");
            sb.append(ids.get(i).getText());
        }
        String funcName = sb.toString();

        List<Expr> args = new ArrayList<>();
        if (ctx.functionArgs() != null && ctx.functionArgs().value() != null) {
            for (SqlWhereParser.ValueContext v : ctx.functionArgs().value()) {
                args.add(visit(v));
            }
        }
        return new FuncExpr(funcName, args);
    }

    @Override
    public Expr visitFieldValue(SqlWhereParser.FieldValueContext ctx) {
        String token = ctx.DQIDENT().getText();
        String name = token.substring(1, token.length() - 1).replaceAll("\\\\\"", "\"");
        return new FieldExpr(name);
    }

    @Override
    public Expr visitStringValue(SqlWhereParser.StringValueContext ctx) {
        String tk = ctx.STRING().getText();
        String s = tk.substring(1, tk.length() - 1).replace("\\'", "'");
        return new StringExpr(s);
    }

    @Override
    public Expr visitNumberValue(SqlWhereParser.NumberValueContext ctx) {
        return new NumberExpr(ctx.NUMBER().getText());
    }

    @Override
    public Expr visitCastValue(SqlWhereParser.CastValueContext ctx) {
        Expr inner = visit(ctx.value());
        String asType = ctx.IDENT().getText();
        return new CastExpr(inner, asType);
    }
}
