package com.gpb.datafirewall.parser.ast;

import java.util.List;

public final class CaseExpr implements Expr {
    public final List<Branch> branches;
    public final Expr elseExpr;

    public CaseExpr(List<Branch> branches, Expr elseExpr) {
        this.branches = List.copyOf(branches);
        this.elseExpr = elseExpr;
    }

    public static final class Branch {
        public final Expr condition;
        public final Expr result;

        public Branch(Expr condition, Expr result) {
            this.condition = condition;
            this.result = result;
        }
    }
}
