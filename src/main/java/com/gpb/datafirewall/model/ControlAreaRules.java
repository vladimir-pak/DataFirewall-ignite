package com.gpb.datafirewall.model;

import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ControlAreaRules {
    private String controlArea;
    private Map<String, Set<Integer>> controls;
}
