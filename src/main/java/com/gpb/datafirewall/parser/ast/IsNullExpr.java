package com.gpb.datafirewall.parser.ast;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class IsNullExpr implements Expr {
    public final Expr expr;
}
