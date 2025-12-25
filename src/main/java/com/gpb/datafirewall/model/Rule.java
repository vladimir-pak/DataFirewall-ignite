package com.gpb.datafirewall.model;

public interface Rule {
    public boolean apply(java.util.Map<String, String> data);
}
