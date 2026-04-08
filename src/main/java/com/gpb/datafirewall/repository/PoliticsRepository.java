package com.gpb.datafirewall.repository;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class PoliticsRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PoliticsRepository(@Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate, 
                            ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, String> getDataset2ControlArea() {
        String sql = """
            SELECT dataset_code, control_area
            FROM datafirewall.dataset2control_area
        """;

        return jdbcTemplate.query(sql, rs -> {
            Map<String, String> result = new HashMap<>();

            while (rs.next()) {
                result.put(
                    rs.getString("dataset_code"),
                    rs.getString("control_area")
                );
            }

            return result;
        });
    }

    public Map<String, String> getErrorMessages() {
        String sql = """
            SELECT 'Rule' || id::varchar as id, error_descr
            FROM datafirewall.dqchecks
            WHERE status = 'enabled';
        """;

        return jdbcTemplate.query(sql, rs -> {
            Map<String, String> result = new HashMap<>();

            while (rs.next()) {
                result.put(
                    rs.getString("id"),
                    rs.getString("error_descr")
                );
            }

            return result;
        });
    }

    public Map<String, Boolean> getFilterFlags() {
        String sql = """
            select ca.code as control_area, coalesce(ff.value, 'N') as filter_flag
            from datafirewall.dictionary_items ca
            left join datafirewall.dictionary_items ff 
                on ff.dictionary_uuid = '9be1b8d7-1c66-4b92-8072-fcabc2b07d5d' 
                and ff.code = ca.code
                and ff.is_active = true
            where ca.dictionary_uuid = '2336658d-9360-47b5-b6da-17f0be5f4574';
        """;

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Boolean> result = new HashMap<>();

            while (rs.next()) {
                result.put(
                    rs.getString("control_area"),
                    rs.getString("filter_flag").equals("Y")
                );
            }

            return result;
        });
    }

    public Map<String, Set<String>> getDatasetExclusion() {
        String sql = """
            SELECT control_area, jsonb_agg(dataset_code order by dataset_code) as dataset_codes
            FROM datafirewall.dataset_exclusion
            GROUP BY control_area;
        """;

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Set<String>> result = new HashMap<>();

            while (rs.next()) {
                String controlArea = rs.getString("control_area");
                String datasetCodes = rs.getString("dataset_codes");

                try {
                    List<String> temp = objectMapper.readValue(
                        datasetCodes,
                        new TypeReference<List<String>>() {}
                    );

                    Set<String> datasets = new LinkedHashSet<>(temp);

                    result.put(controlArea, datasets);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(
                        "Ошибка при парсинге dataset_codes для control_area=" + controlArea,
                        e
                    );
                }
            }

            return result;
        });
    }

    public Map<String, Map<String, Set<String>>> getControlAreaRules() {
        String sql = """
            with fulldata as (
                select dsc.code as dataset_code
                    , ca.code as control_area
                    , regexp_split_to_table(co.value, ';') as control_obj
                    , caic.check_uuid
                from datafirewall.dictionary_items dsc
                join datafirewall.dictionary_items ca
                    on ca.uuid = dsc.parent_id
                    and ca.dictionary_uuid = '2336658d-9360-47b5-b6da-17f0be5f4574'
                join datafirewall.dictionary_items co
                    on co.code = dsc.code
                    and co.dictionary_uuid = '3b392ddb-79b2-421b-bdde-8fffe4e748ee'
                join datafirewall.control_areas_in_check caic 
                    on caic.code = ca.code
                where dsc.dictionary_uuid = '78c525b9-f5e8-40aa-b959-1a22bb9c4983'
            )
            select t.control_area
                , jsonb_object_agg(
                    t.control_obj || '.' || t.checked_attr,
                    ids
                ) as controls
            from (
                select  d.control_area
                    , d.control_obj
                    , ch.checked_attr 
                    , jsonb_agg('Rule' || ch.id::varchar order by ch.id) as ids
                from fulldata d
                join datafirewall.dqchecks ch
                    on ch.uuid = d.check_uuid
                    and ch.control_object = d.control_obj
                    and ch.status = 'enabled'
                group by d.control_area
                    , d.control_obj
                    , ch.checked_attr
            ) t
            group by t.control_area;
        """;

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Map<String, Set<String>>> result = new HashMap<>();

            while (rs.next()) {
                String controlArea = rs.getString("control_area");
                String controlsJson = rs.getString("controls");

                try {
                    Map<String, List<String>> temp = objectMapper.readValue(
                        controlsJson,
                        new TypeReference<Map<String, List<String>>>() {}
                    );

                    Map<String, Set<String>> controls = temp.entrySet().stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> new LinkedHashSet<>(e.getValue())
                        ));

                    result.put(controlArea, controls);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(
                        "Ошибка при парсинге controls для control_area=" + controlArea,
                        e
                    );
                }
            }

            return result;
        });

    }
}
