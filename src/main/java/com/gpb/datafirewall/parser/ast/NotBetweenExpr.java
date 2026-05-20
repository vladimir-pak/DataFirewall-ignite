package com.gpb.datafirewall.parser.ast;

public class NotBetweenExpr implements Expr {
    public final Expr value;
    public final Expr from;
    public final Expr to;

    public NotBetweenExpr(Expr value, Expr from, Expr to) {
        this.value = value;
        this.from = from;
        this.to = to;
    }
}