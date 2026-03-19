package com.gpb.datafirewall.parser.ast;

public abstract class BinaryExpr implements Expr {
    public final Expr left;
    public final Expr right;

    protected BinaryExpr(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }
}
