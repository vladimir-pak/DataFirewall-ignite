package com.gpb.datafirewall.parser.ast;

public class BetweenExpr implements Expr {
    public final Expr value;
    public final Expr from;
    public final Expr to;

    public BetweenExpr(Expr value, Expr from, Expr to) {
        this.value = value;
        this.from = from;
        this.to = to;
    }
}