package com.gpb.datafirewall.parser.ast;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class CastExpr implements Expr {
    public final Expr expr;
    public final String asType;
}
