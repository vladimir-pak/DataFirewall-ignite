package com.gpb.datafirewall.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter 
@Setter 
public class DqChecks {
    @Id
    private Integer id;
    
    @Column(name = "sql_query")
    private String sqlQuery;
}
