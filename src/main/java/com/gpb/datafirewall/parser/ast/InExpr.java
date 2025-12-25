package com.gpb.datafirewall.parser.ast;

import java.util.List;

public class InExpr implements Expr {
    public final Expr value;
    public final List<Expr> options;

    public InExpr(Expr value, List<Expr> options) {
        this.value = value;
        this.options = options;
    }
}

