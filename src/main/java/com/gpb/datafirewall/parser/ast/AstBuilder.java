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
        // Корневое правило: просто возвращаем выражение внутри
        return visit(ctx.expression());
    }

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
    public Expr visitFuncExpr(SqlWhereParser.FuncExprContext ctx) {
        return visit(ctx.functionCall());
    }

    @Override
    public Expr visitLikeExpr(SqlWhereParser.LikeExprContext ctx) {
        Expr val = visit(ctx.value());
        String tk = ctx.STRING().getText();
        String s = tk.substring(1, tk.length()-1).replace("\\'", "'");
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

    @Override
    public Expr visitFuncCall(SqlWhereParser.FuncCallContext ctx) {
        // reconstruct func name with dots
        StringBuilder sb = new StringBuilder();
        List<TerminalNode> ids = ctx.IDENT();
        for (int i = 0; i < ids.size(); i++) {
            if (i>0) sb.append(".");
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
        // remove surrounding double quotes and unescape simple escapes
        String name = token.substring(1, token.length()-1).replaceAll("\\\\\"", "\"");
        return new FieldExpr(name);
    }

    @Override
    public Expr visitStringValue(SqlWhereParser.StringValueContext ctx) {
        String tk = ctx.STRING().getText();
        String s = tk.substring(1, tk.length()-1).replace("\\'", "'");
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
