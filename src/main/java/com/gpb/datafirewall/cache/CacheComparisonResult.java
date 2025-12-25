package com.gpb.datafirewall.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Класс-контейнер с результатами сверки текущего и предыдущего snapshot для определения инкремента.
 * newRecords - новые записи
 * modifiedRecords - обновленные записи
 * deletedRecords - удаленные записи
 */
@Data
@Slf4j
public class CacheComparisonResult {
    private Map<Integer, String> newRecords = new ConcurrentHashMap<>();
    private Map<Integer, String> modifiedRecords = new ConcurrentHashMap<>();
    private Map<Integer, String> deletedRecords = new ConcurrentHashMap<>();
    
    public void addNewRecord(Integer id, String sql) {
        newRecords.put(id, sql);
    }
    
    public void addModifiedRecord(Integer id, String sql) {
        modifiedRecords.put(id, sql);
    }
    
    public void addDeletedRecord(Integer id, String sql) {
        deletedRecords.put(id, sql);
    }

    public Map<Integer, String> getNewRecords() {
        return Collections.unmodifiableMap(new HashMap<>(newRecords));
    }

    public Map<Integer, String> getModifiedRecords() {
        return Collections.unmodifiableMap(new HashMap<>(modifiedRecords));
    }

    public Map<Integer, String> getDeletedRecords() {
        return Collections.unmodifiableMap(new HashMap<>(deletedRecords));
    }

    public Map<Integer, String> getPutRecords() {
        Map<Integer, String> result = new ConcurrentHashMap<>(newRecords);
        result.putAll(modifiedRecords);
        return Collections.unmodifiableMap(result);
    }
    
    public boolean hasChanges() {
        return !newRecords.isEmpty() || !modifiedRecords.isEmpty() || !deletedRecords.isEmpty();
    }
}
