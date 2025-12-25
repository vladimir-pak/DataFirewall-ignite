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
@Table(name = "sql_expressions")
public class SqlExpression {
    @Id
    private Integer id;
    private String sql;
    @Column(name = "source_name")
    private String sourceName;
}
