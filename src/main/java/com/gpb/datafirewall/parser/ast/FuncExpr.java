package com.gpb.datafirewall.parser.ast;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class FuncExpr implements Expr {
    public final String funcName; // e.g. syslib.utf8_length or regexp_like
    public final List<Expr> args;
}
