package com.gpb.datafirewall.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "dqchecks", schema = "datafirewall")
public class DqChecks {
    @Id
    private Integer id;
    
    @Column(name = "sql_query")
    private String sqlQuery;
}
