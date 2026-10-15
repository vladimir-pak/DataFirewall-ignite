package com.gpb.datafirewall.repository;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public Map<String, Map<String, String>> getErrorMessages() {
        String sql = """
            SELECT 
                'Rule' || id::varchar AS id,
                error_descr,
                logic_classif AS classification
            FROM datafirewall.dqchecks
            WHERE status = 'enabled'
            """;

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Map<String, String>> result = new LinkedHashMap<>();

            while (rs.next()) {
                String id = rs.getString("id");

                Map<String, String> idInfo = new LinkedHashMap<>();
                idInfo.put("errorDescription", rs.getString("error_descr"));
                idInfo.put("classification", rs.getString("classification"));

                result.put(id, idInfo);
            }

            return result;
        });
    }

    public Map<String, Boolean> getFilterFlags() {
        String sql = """
            select ca.code as control_area
                , coalesce(flt.value, 'N') as filter_flag
            from datafirewall.dictionary_items ca
            join datafirewall.dictionaries d
                on ca.dictionary_uuid = d.uuid
                and d.code = 'MKD_CONTROL_AREA'
            left join (
                select ff.value, ff.code
                from datafirewall.dictionaries df
                join datafirewall.dictionary_items ff 
                    on df.uuid = ff.dictionary_uuid
                where df.code = 'MKD_FILTER_CONTROL_AREAS' 
                and ff.is_active = true
            ) flt
                on flt.code = ca.code;
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
            select di.code as control_area
                , jsonb_agg(di.value order by di.value)
            from datafirewall.dictionary_items di
            join datafirewall.dictionaries d
            on di.dictionary_uuid = d.uuid
            where d.code = 'DATASET_EXCLUSION'
            group by di.code;
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
                join datafirewall.dictionaries dd
					on dsc.dictionary_uuid = dd.uuid 
					and dd.code = 'MKD_DATASETS'
                join datafirewall.dictionary_items ca
                    on ca.uuid = dsc.parent_id
               	join datafirewall.dictionaries dic
					on ca.dictionary_uuid = dic.uuid 
                    and dic.code = 'MKD_CONTROL_AREA'
                join datafirewall.dictionary_items co
                    on co.code = dsc.code
                join datafirewall.dictionaries dco
					on co.dictionary_uuid = dco.uuid 
                    and dco.code = 'MKD_DATASET_CONTROL_OBJS'
                join datafirewall.control_areas_in_check caic 
                    on caic.code = ca.code
            )
            select t.control_area
                , jsonb_object_agg(
                    t.control_obj || '.' || t.checked_attr,
                    ids
                ) as controls
            from (
                select 
                	d.control_area
                    , d.control_obj
                    , ch.checked_attr 
                    , jsonb_agg(distinct 'Rule' || ch.id::varchar) as ids
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

    /**
     * Вспомогательный кэш для определения кода ПОЛа.
     */
    public Map<String, String> getGenderMapping() {
        String sql = """
            select di.code, di.name as mapped_code
            from datafirewall.dictionary_items di
            join datafirewall.dictionaries d
            on d.uuid = di.dictionary_uuid
            where d.code = 'GENDER_MAPPING'
        """;

        return jdbcTemplate.query(sql, rs -> {
            Map<String, String> result = new HashMap<>();

            while (rs.next()) {
                result.put(
                    rs.getString("code"),
                    rs.getString("mapped_code")
                );
            }

            return result;
        });
    }

    /**
     * Вспомогательный кэш для определения кода ТИП ДУЛ.
     */
    public Map<String, String> getTypeDulMapping() {
        String sql = """
            select di.code, di.name as mapped_code
            from datafirewall.dictionary_items di
            join datafirewall.dictionaries d
            on d.uuid = di.dictionary_uuid
            where d.code = 'TYPE_DUL_MAPPING'
        """;

        return jdbcTemplate.query(sql, rs -> {
            Map<String, String> result = new HashMap<>();

            while (rs.next()) {
                result.put(
                    rs.getString("code"),
                    rs.getString("mapped_code")
                );
            }

            return result;
        });
    }
}
