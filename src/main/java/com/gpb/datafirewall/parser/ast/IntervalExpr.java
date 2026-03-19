package com.gpb.datafirewall.parser.ast;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class IntervalExpr implements Expr {
    public final String literal; // например "20 years"
}