package com.gpb.datafirewall.parser.ast;

import com.gpb.datafirewall.rules.parser.SqlWhereBaseVisitor;
import com.gpb.datafirewall.rules.parser.SqlWhereParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.regex.Pattern;

public class AstBuilder extends SqlWhereBaseVisitor<Expr> {

    // эвристика: если содержимое "..." похоже на regex-паттерн — это строка, а не поле
    private static final Pattern REGEX_LIKE = Pattern.compile(
            "^(\\^|\\.\\*)|.*(\\[|\\]|\\{|\\}|\\\\d|\\\\s|\\||\\$|\\(|\\)|\\+|\\?|\\*).*"
    );

    @Override
    public Expr visitParse(SqlWhereParser.ParseContext ctx) {
        return visit(ctx.expression());
    }

    // --------------------
    // Boolean layer
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

    // --------------------
    // Predicates
    // --------------------

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
        String pattern = unquoteSingle(ctx.STRING().getText());
        return new LikeExpr(val, pattern);
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
    public Expr visitRegexpExpr(SqlWhereParser.RegexpExprContext ctx) {
        Expr left = visit(ctx.value());
        Expr pattern;

        if (ctx.STRING() != null) {
            pattern = new StringExpr(unquoteSingle(ctx.STRING().getText()));
        } else {
            String raw = unquoteDouble(ctx.DQIDENT().getText()).replace("\\\"", "\"");
            pattern = new StringExpr(raw);
        }

        return new FuncExpr("regexp_like", List.of(left, pattern));
    }

    @Override
    public Expr visitFuncPredicateExpr(SqlWhereParser.FuncPredicateExprContext ctx) {
        return visit(ctx.functionCall());
    }

    // --------------------
    // Value/arithmetic layer
    // --------------------

    @Override
    public Expr visitValueExpr(SqlWhereParser.ValueExprContext ctx) {
        return visit(ctx.additiveExpr());
    }

    @Override
    public Expr visitAddExpr(SqlWhereParser.AddExprContext ctx) {
        return new AddExpr(visit(ctx.additiveExpr()), visit(ctx.multiplicativeExpr()));
    }

    @Override
    public Expr visitSubExpr(SqlWhereParser.SubExprContext ctx) {
        return new SubExpr(visit(ctx.additiveExpr()), visit(ctx.multiplicativeExpr()));
    }

    @Override
    public Expr visitToMul(SqlWhereParser.ToMulContext ctx) {
        return visit(ctx.multiplicativeExpr());
    }

    @Override
    public Expr visitMulExpr(SqlWhereParser.MulExprContext ctx) {
        return new MulExpr(visit(ctx.multiplicativeExpr()), visit(ctx.unaryExpr()));
    }

    @Override
    public Expr visitDivExpr(SqlWhereParser.DivExprContext ctx) {
        return new DivExpr(visit(ctx.multiplicativeExpr()), visit(ctx.unaryExpr()));
    }

    @Override
    public Expr visitModExpr(SqlWhereParser.ModExprContext ctx) {
        return new ModExpr(visit(ctx.multiplicativeExpr()), visit(ctx.unaryExpr()));
    }

    @Override
    public Expr visitToUnary(SqlWhereParser.ToUnaryContext ctx) {
        return visit(ctx.unaryExpr());
    }

    @Override
    public Expr visitUnaryMinusExpr(SqlWhereParser.UnaryMinusExprContext ctx) {
        return new UnaryMinusExpr(visit(ctx.unaryExpr()));
    }

    @Override
    public Expr visitToAtom(SqlWhereParser.ToAtomContext ctx) {
        return visit(ctx.atom());
    }

    // --------------------
    // Atoms
    // --------------------

    @Override
    public Expr visitFieldValue(SqlWhereParser.FieldValueContext ctx) {
        String inner = unquoteDouble(ctx.DQIDENT().getText()).replace("\\\"", "\"");

        if (REGEX_LIKE.matcher(inner).matches()) {
            return new StringExpr(inner);
        }
        return new FieldExpr(inner);
    }

    @Override
    public Expr visitNumberValue(SqlWhereParser.NumberValueContext ctx) {
        return new NumberExpr(ctx.NUMBER().getText());
    }

    @Override
    public Expr visitStringValue(SqlWhereParser.StringValueContext ctx) {
        return new StringExpr(unquoteSingle(ctx.STRING().getText()));
    }

    @Override
    public Expr visitFuncValue(SqlWhereParser.FuncValueContext ctx) {
        return visit(ctx.functionCall());
    }

    @Override
    public Expr visitCastValue(SqlWhereParser.CastValueContext ctx) {
        Expr inner = visit(ctx.value());
        String asType = ctx.IDENT().getText();
        return new CastExpr(inner, asType);
    }

    @Override
    public Expr visitExtractValue(SqlWhereParser.ExtractValueContext ctx) {
        String unit = ctx.IDENT().getText();
        Expr fromExpr = visit(ctx.value());
        return new FuncExpr("extract", List.of(new StringExpr(unit), fromExpr));
    }

    @Override
    public Expr visitIntervalValue(SqlWhereParser.IntervalValueContext ctx) {
        String lit = unquoteSingle(ctx.STRING().getText());
        return new IntervalExpr(lit);
    }

    @Override
    public Expr visitIdentValue(SqlWhereParser.IdentValueContext ctx) {
        String name = ctx.IDENT().getText();
        return new FuncExpr(name, Collections.emptyList());
    }

    @Override
    public Expr visitValueParen(SqlWhereParser.ValueParenContext ctx) {
        return visit(ctx.value());
    }

    // --------------------
    // Functions
    // --------------------

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

    // --------------------
    // Helpers
    // --------------------

    private static String unquoteSingle(String tk) {
        String s = tk.substring(1, tk.length() - 1);
        s = s.replace("''", "'");
        s = s.replace("\\'", "'");
        return s;
    }

    private static String unquoteDouble(String tk) {
        return tk.substring(1, tk.length() - 1);
    }
}