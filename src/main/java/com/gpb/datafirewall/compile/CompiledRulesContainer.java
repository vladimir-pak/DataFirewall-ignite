package com.gpb.datafirewall.compile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.gpb.datafirewall.model.Rule;

/**
 * Класс-контейнер для хранения Map с экземплярами классов проверок
 * AtomicReference - активный snapshot для атомарности замены нового snapshot с новыми экземплярами
 */
@Component
public class CompiledRulesContainer {

    // Храним «активный снапшот» правил
    private final AtomicReference<Map<String, Rule>> rulesRef =
            new AtomicReference<>(Map.of()); // пустая неизменяемая Map

    public Rule getRule(String name) {
        return rulesRef.get().get(name);
    }

    /**
     * Получение всей Map с экземплярами проверок
     * @return Map с ключом Rule{идентификатор проверки}, например Rule123
     */
    public Map<String, Rule> snapshot() {
        return rulesRef.get(); // только для чтения!
    }

    // Полная замена всех правил
    public void replaceAll(Map<String, Rule> newRules) {
        // на всякий случай создаём копию и делаем её неизменяемой
        Map<String, Rule> copy = Collections.unmodifiableMap(new HashMap<>(newRules));
        rulesRef.set(copy); // атомарная подмена
    }
}
