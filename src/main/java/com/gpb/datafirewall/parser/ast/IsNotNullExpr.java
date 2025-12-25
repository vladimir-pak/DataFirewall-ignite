package com.gpb.datafirewall.parser.ast;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class IsNotNullExpr implements Expr {
    public final Expr expr;
}
