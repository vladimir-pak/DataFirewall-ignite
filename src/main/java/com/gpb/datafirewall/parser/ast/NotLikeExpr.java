package com.gpb.datafirewall.parser.ast;

public class NotLikeExpr implements Expr {
    public final Expr value;
    public final String pattern;

    public NotLikeExpr(Expr value, String pattern) {
        this.value = value;
        this.pattern = pattern;
    }
}