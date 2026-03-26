package com.gpb.datafirewall.repository;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.gpb.datafirewall.model.PgStat;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PgStatRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<PgStat> findTableStats(List<String> tables, String schema) {
        String sql = """
            select
                schemaname || '.' || relname as table_name,
                (coalesce(n_tup_ins, 0) + coalesce(n_tup_upd, 0) + coalesce(n_tup_del, 0)) as total_changes
            from pg_stat_user_tables
            where relname in (:tables)
            and schemaname = :schema
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tables", tables)
                .addValue("schema", schema);

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                new PgStat(
                        rs.getString("table_name"),
                        rs.getLong("total_changes")
                )
        );
    }
}
