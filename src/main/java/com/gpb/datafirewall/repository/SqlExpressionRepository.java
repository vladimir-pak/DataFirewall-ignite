package com.gpb.datafirewall.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gpb.datafirewall.model.SqlExpression;

public interface SqlExpressionRepository extends JpaRepository<SqlExpression, Integer> {
    List<SqlExpression> findBySourceName(String sourceName);
}
