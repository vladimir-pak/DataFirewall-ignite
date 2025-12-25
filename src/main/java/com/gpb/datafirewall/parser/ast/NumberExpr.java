package com.gpb.datafirewall.parser.ast;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class NumberExpr implements Expr {
    public final String value; // keep as string, parse later
}
