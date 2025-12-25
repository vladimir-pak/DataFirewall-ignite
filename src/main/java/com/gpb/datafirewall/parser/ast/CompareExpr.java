package com.gpb.datafirewall.parser.ast;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class CompareExpr implements Expr {
    public final Expr left;
    public final String op;
    public final Expr right;
}
