package com.gpb.datafirewall.parser;

import java.util.regex.Pattern;

public final class SqlTextNormalizer {

    private static final Pattern INVISIBLE = Pattern.compile("[\\u00A0\\u200B\\uFEFF]"); // NBSP, ZWSP, BOM
    private static final Pattern MULTISPACE = Pattern.compile("[ \\t]{2,}");

    private SqlTextNormalizer() {}

    public static String normalize(String sql) {
        if (sql == null) return null;

        String s = sql;

        // невидимые символы и переносы
        s = INVISIBLE.matcher(s).replaceAll(" ");
        s = s.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');

        // частая проблема копипаста: ""ДУЛ... -> "ДУЛ...
        s = s.replaceAll("(^|[^\\\"])\"\"([A-Za-zА-Яа-яЁё_])", "$1\"$2");

        // AND( / OR( / NOT(
        s = s.replaceAll("\\b(AND|OR|NOT)\\(", "$1 (");

        // "Номер"is -> "Номер" is
        s = s.replaceAll("\"\\s*(is|IS|like|LIKE|in|IN|and|AND|or|OR|not|NOT)\\b", "\" $1");
        s = s.replaceAll("\"(is|IS|like|LIKE|in|IN)\\b", "\" $1");

        // лишние пробелы
        s = MULTISPACE.matcher(s).replaceAll(" ");

        return s.trim();
    }
}