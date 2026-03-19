package com.gpb.datafirewall.parser.ast;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class IdentExpr implements Expr {
    public final String name;
}
