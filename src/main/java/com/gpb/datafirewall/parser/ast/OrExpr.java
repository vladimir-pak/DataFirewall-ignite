package com.gpb.datafirewall.parser.ast;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class OrExpr implements Expr {
    public final Expr left, right;
}
