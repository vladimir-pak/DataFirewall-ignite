package com.gpb.datafirewall.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class RuleList implements Serializable  {
    private List<Integer> ids;
}
