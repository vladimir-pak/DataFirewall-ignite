package com.gpb.datafirewall.parser.ast;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class FieldExpr implements Expr {
    public final String name;
}