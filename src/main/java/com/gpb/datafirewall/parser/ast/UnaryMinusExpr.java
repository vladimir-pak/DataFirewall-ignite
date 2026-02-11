package com.gpb.datafirewall.parser.ast;

public class UnaryMinusExpr implements Expr {
    public final Expr expr;

    public UnaryMinusExpr(Expr expr) {
        this.expr = expr;
    }
}
