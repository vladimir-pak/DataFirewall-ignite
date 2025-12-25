package com.gpb.datafirewall.parser.ast;

public class LikeExpr implements Expr {
    public final Expr value;
    public final String pattern;

    public LikeExpr(Expr value, String pattern) {
        this.value = value;
        this.pattern = pattern;
    }
}

