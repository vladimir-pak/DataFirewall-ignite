package com.gpb.datafirewall.repository;

import com.gpb.datafirewall.model.DqChecks;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

public class DqChecksRepository {

    private final JdbcTemplate jdbcTemplate;

    public DqChecksRepository(
            @Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DqChecks> findAll() {
        String sql = """
            SELECT c.id, c.sql_query
            FROM datafirewall.dqchecks c
            WHERE status = 'enabled'
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new DqChecks(
            rs.getObject("id", Integer.class),
            rs.getString("sql_query")
        ));
    }
}
