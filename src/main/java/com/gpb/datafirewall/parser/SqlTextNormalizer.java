package com.gpb.datafirewall.parser;

/** Normalize copied SQL outside literals and quoted field names only. */
public final class SqlTextNormalizer {
    private SqlTextNormalizer() {}

    public static String normalize(String sql) {
        if (sql == null) return null;
        StringBuilder out = new StringBuilder();
        char quote = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (quote != 0) {
                out.append(c);
                if (c == '\\' && i + 1 < sql.length()) {
                    out.append(sql.charAt(++i));
                } else if (c == quote) {
                    if (quote == '\'' && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                        out.append(sql.charAt(++i));
                    } else quote = 0;
                }
                continue;
            }
            // Retain the existing copy/paste repair for an extra opening quote.
            if (c == '"' && i + 2 < sql.length() && sql.charAt(i + 1) == '"'
                    && (Character.isLetter(sql.charAt(i + 2)) || sql.charAt(i + 2) == '_')) i++;
            if (c == '\'' || c == '"') {
                quote = c;
                out.append(c);
            } else if (Character.isWhitespace(c) || c == '\u00A0' || c == '\u200B' || c == '\uFEFF') {
                if (out.length() > 0 && out.charAt(out.length() - 1) != ' ') out.append(' ');
            } else out.append(c);
        }
        return out.toString().trim();
    }
}
