package com.gpb.datafirewall.parser;

import com.gpb.datafirewall.parser.ast.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Генератор Java Source (код Java в String) на основе AST
 *
 * Важно: Janino НЕ поддерживает switch-expression, поэтому используем if/else.
 */
public class JavaRuleGenerator {
    public static final String GENERATOR_VERSION = "2-date-expressions";
    private static final Logger log = LoggerFactory.getLogger(JavaRuleGenerator.class);
    private static final Pattern INTERVAL_PART = Pattern.compile(
            "\\G\\s*([+-]?\\d+)\\s+(years?|months?|weeks?|days?)\\b", Pattern.CASE_INSENSITIVE);

    public String generate(String className, Expr expr) {
        StringBuilder sb = new StringBuilder();

        sb.append("import java.util.*;\n");
        sb.append("import java.util.regex.*;\n");
        sb.append("import java.time.*;\n");
        sb.append("import java.time.format.*;\n");
        sb.append("import com.gpb.datafirewall.model.Rule;\n");

        sb.append("public class ").append(className).append(" implements Rule {\n");

        sb.append("  private String toStringSafe(Object o){ return o==null?null:o.toString(); }\n");

        sb.append("  private String getField(java.util.Map<String,String> data, String key){\n");
        sb.append("    if(!data.containsKey(key)) {\n");
        sb.append("      throw new java.util.NoSuchElementException(\"Missing field: \" + key);\n");
        sb.append("    }\n");
        sb.append("    return (String) data.get(key);\n");
        sb.append("  }\n");

        sb.append("  private void requireFields(java.util.Map<String,String> data, String... keys){\n");
        sb.append("    if(data == null) {\n");
        sb.append("      throw new java.util.NoSuchElementException(\"Data map is null\");\n");
        sb.append("    }\n");

        sb.append("    for(String key : keys){\n");

        sb.append("      System.out.println(\n");
        sb.append("          \"[RULE-INTERNAL] class=\" + getClass().getName()\n");
        sb.append("          + \" classLoader=\" + getClass().getClassLoader()\n");
        sb.append("          + \" key=[\" + key + \"]\"\n");
        sb.append("          + \" contains=\" + data.containsKey(key)\n");
        sb.append("      );\n");

        sb.append("      if(!data.containsKey(key)) {\n");
        sb.append("        throw new java.util.NoSuchElementException(\"Missing field: \" + key);\n");
        sb.append("      }\n");

        sb.append("    }\n");
        sb.append("  }\n");

        sb.append("  private Double toDoubleOrNull(Object v){\n");
        sb.append("    try { return v==null?null:Double.valueOf(v.toString()); }\n");
        sb.append("    catch(Exception e){ return null; }\n");
        sb.append("  }\n");

        sb.append("  private Integer utf8Length(String s){\n");
        sb.append("    return s==null ? null : Integer.valueOf(s.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);\n");
        sb.append("  }\n");

        sb.append("  private Integer toIntOrNull(Object v){\n");
        sb.append("    try {\n");
        sb.append("      if(v==null) return null;\n");
        sb.append("      String s = v.toString().trim();\n");
        sb.append("      if(s.isEmpty()) return null;\n");
        sb.append("      if(s.endsWith(\".0\")) s = s.substring(0, s.length()-2);\n");
        sb.append("      return Integer.valueOf(s);\n");
        sb.append("    } catch(Exception e){ return null; }\n");
        sb.append("  }\n");

        sb.append("  private String castString(Object v){\n");
        sb.append("    if(v==null) return null;\n");
        sb.append("    if(v instanceof Double){\n");
        sb.append("      double d = ((Double)v).doubleValue();\n");
        sb.append("      if(d == Math.rint(d)) return String.valueOf((long)d);\n");
        sb.append("    }\n");
        sb.append("    if(v instanceof Float){\n");
        sb.append("      float f = ((Float)v).floatValue();\n");
        sb.append("      if(f == Math.rint(f)) return String.valueOf((long)f);\n");
        sb.append("    }\n");
        sb.append("    return v.toString();\n");
        sb.append("  }\n");

        sb.append("  private String substringSql(Object src, Object startObj, Object lenObj){\n");
        sb.append("    String s = toStringSafe(src);\n");
        sb.append("    Integer start = toIntOrNull(startObj);\n");
        sb.append("    Integer len = toIntOrNull(lenObj);\n");
        sb.append("    if(s==null || start==null || len==null) return null;\n");
        sb.append("    if(len <= 0) return \"\";\n");
        sb.append("    int from;\n");
        sb.append("    if(start > 0) from = start - 1;\n");
        sb.append("    else if(start < 0) from = s.length() + start;\n");
        sb.append("    else from = 0;\n");
        sb.append("    if(from < 0 || from >= s.length()) return \"\";\n");
        sb.append("    int to = Math.min(from + len, s.length());\n");
        sb.append("    return s.substring(from, to);\n");
        sb.append("  }\n");

        sb.append("  private boolean regexp_like(Object v, Object p){\n");
        sb.append("    String s = toStringSafe(v);\n");
        sb.append("    String pat = toStringSafe(p);\n");
        sb.append("    if(s==null || pat==null) return false;\n");
        sb.append("    try{ return Pattern.compile(pat).matcher(s).find(); }catch(Exception e){ return false; }\n");
        sb.append("  }\n");

        sb.append("  private LocalDate toDateOrNull(Object v){\n");
        sb.append("    if(v==null) return null;\n");
        sb.append("    String s = v.toString();\n");
        sb.append("    if(s==null) return null;\n");
        sb.append("    s = s.trim();\n");
        sb.append("    if(s.isEmpty()) return null;\n");
        sb.append("    try { return LocalDate.parse(s); }\n");
        sb.append("    catch(DateTimeParseException ex){\n");
        sb.append("      try {\n");
        sb.append("        int idx = s.indexOf('T');\n");
        sb.append("        if(idx > 0) return LocalDate.parse(s.substring(0, idx));\n");
        sb.append("      } catch(Exception ignore) {}\n");
        sb.append("      return null;\n");
        sb.append("    }\n");
        sb.append("  }\n");

        sb.append("  private LocalDate current_date(){ return LocalDate.now(ZoneId.systemDefault()); }\n");
        sb.append("  private LocalDate now(){ return LocalDate.now(ZoneId.systemDefault()); }\n");

        sb.append("  private LocalDate years_add(Object date, Object years){\n");
        sb.append("    LocalDate d = toDateOrNull(date);\n");
        sb.append("    Double y = toDoubleOrNull(years);\n");
        sb.append("    if(d==null || y==null) return null;\n");
        sb.append("    return d.plusYears(y.longValue());\n");
        sb.append("  }\n");

        appendDateExpressionHelpers(sb);

        sb.append("  private String replace(Object src, Object target, Object replacement){\n");
        sb.append("    String s = toStringSafe(src);\n");
        sb.append("    String t = toStringSafe(target);\n");
        sb.append("    String r = toStringSafe(replacement);\n");
        sb.append("    if(s==null || t==null || r==null) return s;\n");
        sb.append("    return s.replace(t, r);\n");
        sb.append("  }\n");

        sb.append("  private String utf8_lower(Object v){\n");
        sb.append("    String s = toStringSafe(v);\n");
        sb.append("    return s==null ? null : s.toLowerCase(Locale.ROOT);\n");
        sb.append("  }\n");

        sb.append("  private String trim(Object v){\n");
        sb.append("    String s = toStringSafe(v);\n");
        sb.append("    return s==null ? null : s.trim();\n");
        sb.append("  }\n");

        sb.append("  private String concat(Object... args){\n");
        sb.append("    StringBuilder sb = new StringBuilder();\n");
        sb.append("    if(args!=null){\n");
        sb.append("      for(Object a : args){\n");
        sb.append("        String s = toStringSafe(a);\n");
        sb.append("        if(s!=null) sb.append(s);\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    return sb.toString();\n");
        sb.append("  }\n");

        sb.append("  private String coalesce(Object... args){\n");
        sb.append("    if(args!=null){\n");
        sb.append("      for(Object a : args){\n");
        sb.append("        if(a!=null) return toStringSafe(a);\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    return null;\n");
        sb.append("  }\n");

        sb.append("  private boolean numEq(Object a, Object b){ Double da=toDoubleOrNull(a), db=toDoubleOrNull(b); return da!=null && db!=null && Double.compare(da,db)==0; }\n");
        sb.append("  private boolean numNe(Object a, Object b){ Double da=toDoubleOrNull(a), db=toDoubleOrNull(b); return da!=null && db!=null && Double.compare(da,db)!=0; }\n");
        sb.append("  private boolean numGt(Object a, Object b){ Double da=toDoubleOrNull(a), db=toDoubleOrNull(b); return da!=null && db!=null && da>db; }\n");
        sb.append("  private boolean numLt(Object a, Object b){ Double da=toDoubleOrNull(a), db=toDoubleOrNull(b); return da!=null && db!=null && da<db; }\n");
        sb.append("  private boolean numGe(Object a, Object b){ Double da=toDoubleOrNull(a), db=toDoubleOrNull(b); return da!=null && db!=null && da>=db; }\n");
        sb.append("  private boolean numLe(Object a, Object b){ Double da=toDoubleOrNull(a), db=toDoubleOrNull(b); return da!=null && db!=null && da<=db; }\n");

        sb.append("  private boolean dateEq(Object a, Object b){ LocalDate da=toDateOrNull(a), db=toDateOrNull(b); return da!=null && db!=null && da.isEqual(db); }\n");
        sb.append("  private boolean dateNe(Object a, Object b){ LocalDate da=toDateOrNull(a), db=toDateOrNull(b); return da!=null && db!=null && !da.isEqual(db); }\n");
        sb.append("  private boolean dateGt(Object a, Object b){ LocalDate da=toDateOrNull(a), db=toDateOrNull(b); return da!=null && db!=null && da.isAfter(db); }\n");
        sb.append("  private boolean dateLt(Object a, Object b){ LocalDate da=toDateOrNull(a), db=toDateOrNull(b); return da!=null && db!=null && da.isBefore(db); }\n");
        sb.append("  private boolean dateGe(Object a, Object b){ LocalDate da=toDateOrNull(a), db=toDateOrNull(b); return da!=null && db!=null && (da.isAfter(db)||da.isEqual(db)); }\n");
        sb.append("  private boolean dateLe(Object a, Object b){ LocalDate da=toDateOrNull(a), db=toDateOrNull(b); return da!=null && db!=null && (da.isBefore(db)||da.isEqual(db)); }\n");

        sb.append("  private Object add(Object a, Object b){\n");
        sb.append("    if(a instanceof Period && b instanceof Period) return ((Period)a).plus((Period)b);\n");
        sb.append("    if(b instanceof Period){ LocalDate d=toDateOrNull(a); return d==null?null:d.plus((Period)b); }\n");
        sb.append("    if(a instanceof Period){ LocalDate d=toDateOrNull(b); return d==null?null:d.plus((Period)a); }\n");
        sb.append("    Double da=toDoubleOrNull(a), db=toDoubleOrNull(b);\n");
        sb.append("    if(da!=null && db!=null) return da+db;\n");
        sb.append("    return null;\n");
        sb.append("  }\n");

        sb.append("  private Object sub(Object a, Object b){\n");
        sb.append("    if(a instanceof Period && b instanceof Period) return ((Period)a).minus((Period)b);\n");
        sb.append("    if(b instanceof Period){ LocalDate d=toDateOrNull(a); return d==null?null:d.minus((Period)b); }\n");
        sb.append("    Double da=toDoubleOrNull(a), db=toDoubleOrNull(b);\n");
        sb.append("    if(da!=null && db!=null) return da-db;\n");
        sb.append("    return null;\n");
        sb.append("  }\n");

        sb.append("  private boolean isIntegralObject(Object v){\n");
        sb.append("    if(v instanceof Byte || v instanceof Short || v instanceof Integer || v instanceof Long) return true;\n");
        sb.append("    if(v instanceof String) return ((String)v).matches(\"-?\\\\d+\");\n");
        sb.append("    return false;\n");
        sb.append("  }\n");

        sb.append("  private Object mul(Object a, Object b){ Double da=toDoubleOrNull(a), db=toDoubleOrNull(b); if(da!=null && db!=null) return da*db; return null; }\n");
        sb.append("  private Object div(Object a, Object b){ Double da=toDoubleOrNull(a), db=toDoubleOrNull(b); if(da!=null && db!=null && db!=0d) return da/db; return null; }\n");
        sb.append("  private Object mod(Object a, Object b){\n");
        sb.append("    if(isIntegralObject(a) && isIntegralObject(b)){\n");
        sb.append("      Long la = toLongOrNull(a);\n");
        sb.append("      Long lb = toLongOrNull(b);\n");
        sb.append("      if(la!=null && lb!=null && lb!=0L) return la % lb;\n");
        sb.append("    }\n");
        sb.append("    Double da=toDoubleOrNull(a), db=toDoubleOrNull(b);\n");
        sb.append("    if(da!=null && db!=null && db!=0d) return da%db;\n");
        sb.append("    return null;\n");
        sb.append("  }\n");

        sb.append("  private String sqlLikeToRegex(String pattern){\n");
        sb.append("    if(pattern==null) return null;\n");
        sb.append("    StringBuilder out = new StringBuilder();\n");
        sb.append("    out.append(\"^\");\n");
        sb.append("    for(int i=0;i<pattern.length();i++){\n");
        sb.append("      char c = pattern.charAt(i);\n");
        sb.append("      if(c=='%'){\n");
        sb.append("        out.append(\".*\");\n");
        sb.append("      } else if(c=='_'){\n");
        sb.append("        out.append(\".\");\n");
        sb.append("      } else {\n");
        sb.append("        if(\"\\\\.[]{}()+-^$?|*\".indexOf(c) >= 0){\n");
        sb.append("          out.append(\"\\\\\\\\\");\n");
        sb.append("        }\n");
        sb.append("        out.append(c);\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    out.append(\"$\");\n");
        sb.append("    return out.toString();\n");
        sb.append("  }\n");

        sb.append("  private boolean likeSql(Object value, String pattern){\n");
        sb.append("    String s = toStringSafe(value);\n");
        sb.append("    if(s==null || pattern==null) return false;\n");
        sb.append("    try{\n");
        sb.append("      return Pattern.compile(sqlLikeToRegex(pattern), Pattern.DOTALL).matcher(s).matches();\n");
        sb.append("    } catch(Exception e){ return false; }\n");
        sb.append("  }\n");

        sb.append("  private boolean notLikeSql(Object value, String pattern){\n");
        sb.append("    String s = toStringSafe(value);\n");
        sb.append("    if(s==null || pattern==null) return false;\n");
        sb.append("    try{\n");
        sb.append("      return !Pattern.compile(sqlLikeToRegex(pattern), Pattern.DOTALL).matcher(s).matches();\n");
        sb.append("    } catch(Exception e){ return false; }\n");
        sb.append("  }\n");

        sb.append("  private boolean inSql(Object value, Object... options){\n");
        sb.append("    if(value==null || options==null) return false;\n");
        sb.append("    for(Object option : options){\n");
        sb.append("      if(option!=null && java.util.Objects.equals(value, option)) return true;\n");
        sb.append("    }\n");
        sb.append("    return false;\n");
        sb.append("  }\n");

        sb.append("  private boolean notInSql(Object value, Object... options){\n");
        sb.append("    if(value==null || options==null) return false;\n");
        sb.append("    boolean hasNullOption = false;\n");
        sb.append("    for(Object option : options){\n");
        sb.append("      if(option==null){\n");
        sb.append("        hasNullOption = true;\n");
        sb.append("        continue;\n");
        sb.append("      }\n");
        sb.append("      if(java.util.Objects.equals(value, option)) return false;\n");
        sb.append("    }\n");
        sb.append("    if(hasNullOption) return false;\n");
        sb.append("    return true;\n");
        sb.append("  }\n");

        sb.append("  private boolean betweenSql(Object value, Object from, Object to){\n");
        sb.append("    if(value==null || from==null || to==null) return false;\n");
        sb.append("    Double v = toDoubleOrNull(value);\n");
        sb.append("    Double f = toDoubleOrNull(from);\n");
        sb.append("    Double t = toDoubleOrNull(to);\n");
        sb.append("    if(v!=null && f!=null && t!=null){\n");
        sb.append("      return v >= f && v <= t;\n");
        sb.append("    }\n");
        sb.append("    LocalDate dv = toDateOrNull(value);\n");
        sb.append("    LocalDate df = toDateOrNull(from);\n");
        sb.append("    LocalDate dt = toDateOrNull(to);\n");
        sb.append("    if(dv!=null && df!=null && dt!=null){\n");
        sb.append("      return !dv.isBefore(df) && !dv.isAfter(dt);\n");
        sb.append("    }\n");
        sb.append("    String sv = toStringSafe(value);\n");
        sb.append("    String sf = toStringSafe(from);\n");
        sb.append("    String st = toStringSafe(to);\n");
        sb.append("    if(sv==null || sf==null || st==null) return false;\n");
        sb.append("    return sv.compareTo(sf) >= 0 && sv.compareTo(st) <= 0;\n");
        sb.append("  }\n");

        sb.append("  private boolean notBetweenSql(Object value, Object from, Object to){\n");
        sb.append("    if(value==null || from==null || to==null) return false;\n");
        sb.append("    return !betweenSql(value, from, to);\n");
        sb.append("  }\n");

        sb.append("  private Long toLongOrNull(Object v){\n");
        sb.append("    try {\n");
        sb.append("      if(v==null) return null;\n");
        sb.append("      String s = v.toString().trim();\n");
        sb.append("      if(s.isEmpty()) return null;\n");
        sb.append("      if(s.endsWith(\".0\")) s = s.substring(0, s.length()-2);\n");
        sb.append("      return Long.valueOf(s);\n");
        sb.append("    } catch(Exception e){ return null; }\n");
        sb.append("  }\n");

        Set<String> requiredFields = new LinkedHashSet<>();
        // Во время генерации Java-выражения одновременно собираем все FieldExpr
        String condition = genBool(expr, requiredFields);

        sb.append("  public boolean apply(java.util.Map<String,String> data){\n");
        sb.append("    try{\n");

        if (!requiredFields.isEmpty()) {
            sb.append("      requireFields(data");

            for (String field : requiredFields) {
                sb.append(",\"")
                        .append(escapeJava(field))
                        .append("\"");
            }

            sb.append(");\n");
        }

        sb.append("      return ").append(condition).append(";\n");
        sb.append("    } catch(java.util.NoSuchElementException e){\n");
        sb.append("      throw e;\n");
        sb.append("    } catch(Exception e){\n");
        sb.append("      return false;\n");
        sb.append("    }\n");
        sb.append("  }\n");

        sb.append("}\n");

        log.debug(sb.toString());
        return sb.toString();
    }

    /** Date-only helpers emitted into every Rule, without extra runtime classes. */
    private void appendDateExpressionHelpers(StringBuilder sb) {
        sb.append("""
                  private Period ageSql(Object endValue, Object startValue){
                    LocalDate end = toDateOrNull(endValue);
                    LocalDate start = toDateOrNull(startValue);
                    if(end==null || start==null) return null;
                    int sign = end.isBefore(start) ? -1 : 1;
                    LocalDate earlier = sign < 0 ? end : start;
                    LocalDate later = sign < 0 ? start : end;
                    int years = later.getYear() - earlier.getYear();
                    int months = later.getMonthValue() - earlier.getMonthValue();
                    int days = later.getDayOfMonth() - earlier.getDayOfMonth();
                    // PostgreSQL AGE borrows days from the earlier date's month.
                    if(days < 0){ days += earlier.lengthOfMonth(); months--; }
                    if(months < 0){ months += 12; years--; }
                    return Period.of(sign * years, sign * months, sign * days);
                  }
                  private Long extractSql(String unit, Object value){
                    if(value==null) return null;
                    if(value instanceof Period){
                      Period p = (Period)value;
                      long months = p.toTotalMonths();
                      if("year".equals(unit)) return Long.valueOf(months / 12);
                      if("month".equals(unit)) return Long.valueOf(months % 12);
                      if("day".equals(unit)) return Long.valueOf(p.getDays());
                      throw new IllegalArgumentException("Unsupported EXTRACT unit for interval: " + unit);
                    }
                    LocalDate d = toDateOrNull(value);
                    if(d==null) return null;
                    if("year".equals(unit)) return Long.valueOf(d.getYear());
                    if("month".equals(unit)) return Long.valueOf(d.getMonthValue());
                    if("day".equals(unit)) return Long.valueOf(d.getDayOfMonth());
                    if("quarter".equals(unit)) return Long.valueOf((d.getMonthValue() - 1) / 3 + 1);
                    if("dow".equals(unit)) return Long.valueOf(d.getDayOfWeek().getValue() % 7);
                    if("isodow".equals(unit)) return Long.valueOf(d.getDayOfWeek().getValue());
                    if("doy".equals(unit)) return Long.valueOf(d.getDayOfYear());
                    if("week".equals(unit)) return Long.valueOf(d.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR));
                    if("isoyear".equals(unit)) return Long.valueOf(d.get(java.time.temporal.IsoFields.WEEK_BASED_YEAR));
                    throw new IllegalArgumentException("Unsupported EXTRACT unit for date: " + unit);
                  }
                  private boolean compareIntervalSql(Object left, String op, Object right){
                    if(left==null || right==null) return false;
                    Period a = (Period)left;
                    Period b = (Period)right;
                    // PostgreSQL interval comparisons treat a month as 30 days.
                    int cmp = Long.compare(a.toTotalMonths() * 30 + a.getDays(),
                                           b.toTotalMonths() * 30 + b.getDays());
                    if("=".equals(op)) return cmp==0;
                    if("!=".equals(op) || "<>".equals(op)) return cmp!=0;
                    if(">".equals(op)) return cmp>0;
                    if("<".equals(op)) return cmp<0;
                    if(">=".equals(op)) return cmp>=0;
                    if("<=".equals(op)) return cmp<=0;
                    throw new IllegalArgumentException("Unsupported interval comparison: " + op);
                  }
                """);
    }

    private boolean isIntervalExpr(Expr e) {
        if (e instanceof IntervalExpr) return true;
        if (e instanceof FuncExpr f) return "age".equalsIgnoreCase(f.funcName);
        if (e instanceof AddExpr a) return isIntervalExpr(a.left) && isIntervalExpr(a.right);
        if (e instanceof SubExpr s) return isIntervalExpr(s.left) && isIntervalExpr(s.right);
        if (e instanceof UnaryMinusExpr u) return isIntervalExpr(u.expr);
        return false;
    }

    private void requireArity(FuncExpr f, int min, int max) {
        int actual = f.args == null ? 0 : f.args.size();
        if (actual < min || actual > max) {
            throw new IllegalStateException("Function " + f.funcName + " expects "
                    + (min == max ? String.valueOf(min) : min + ".." + max)
                    + " argument(s), got " + actual);
        }
    }

    private String extractUnit(Expr expr) {
        if (expr instanceof StringExpr s) return s.value.trim().toLowerCase(Locale.ROOT);
        if (expr instanceof IdentExpr i) return i.name.trim().toLowerCase(Locale.ROOT);
        throw new IllegalStateException("EXTRACT/DATE_PART unit must be a constant");
    }

    private void validateExtractUnit(String unit, boolean interval) {
        if (unit.equals("year") || unit.equals("month") || unit.equals("day")) return;
        if (!interval && (unit.equals("quarter") || unit.equals("dow") || unit.equals("isodow")
                || unit.equals("doy") || unit.equals("week") || unit.equals("isoyear"))) return;
        throw new IllegalStateException("Unsupported EXTRACT/DATE_PART unit for "
                + (interval ? "interval: " : "date: ") + unit);
    }

    /** Validate literal intervals during generation instead of silently returning null at runtime. */
    private String genInterval(String literal) {
        String text = literal == null ? "" : literal.trim();
        Matcher matcher = INTERVAL_PART.matcher(text);
        java.time.Period period = java.time.Period.ZERO;
        int end = 0;
        try {
            while (matcher.find()) {
                int count = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2).toLowerCase(Locale.ROOT);
                if (unit.startsWith("year")) period = period.plusYears(count);
                else if (unit.startsWith("month")) period = period.plusMonths(count);
                else if (unit.startsWith("week")) period = period.plusDays(Math.multiplyExact(count, 7));
                else period = period.plusDays(count);
                end = matcher.end();
            }
        } catch (ArithmeticException | NumberFormatException ex) {
            throw new IllegalStateException("INTERVAL is out of range: " + literal, ex);
        }
        if (end == 0 || end != text.length()) {
            throw new IllegalStateException("Unsupported INTERVAL: " + literal
                    + ". Expected integer years, months, weeks or days.");
        }
        return "Period.of(" + period.getYears() + "," + period.getMonths() + "," + period.getDays() + ")";
    }

    private boolean isNumericExpr(Expr e) {
        if (e instanceof NumberExpr) return true;

        if (e instanceof CastExpr c) {
            String type = c.asType == null ? "" : c.asType.toLowerCase(Locale.ROOT);
            return type.equals("int")
                    || type.equals("integer")
                    || type.equals("bigint")
                    || type.equals("long")
                    || type.equals("double")
                    || type.equals("float")
                    || type.equals("decimal")
                    || type.equals("numeric");
        }

        if (e instanceof FuncExpr f) {
            String fn = f.funcName.toLowerCase(Locale.ROOT);
            return fn.equals("syslib.utf8_length")
                    || fn.equals("utf8_length")
                    || fn.equals("length")
                    || fn.equals("extract")
                    || fn.equals("date_part");
        }

        return (e instanceof AddExpr)
                || (e instanceof SubExpr)
                || (e instanceof MulExpr)
                || (e instanceof DivExpr)
                || (e instanceof ModExpr)
                || (e instanceof UnaryMinusExpr);
    }

    private boolean isDateLikeExpr(Expr e) {
        if (e instanceof FuncExpr f) {
            String fn = f.funcName.toLowerCase(Locale.ROOT);
            if (fn.equals("current_date") || fn.equals("now") || fn.equals("years_add")) return true;
        }

        if (e instanceof CastExpr c) {
            String type = c.asType == null ? "" : c.asType.toLowerCase(Locale.ROOT);
            return type.equals("date")
                    || type.equals("timestamp")
                    || type.equals("datetime");
        }

        if (e instanceof FieldExpr fe) {
            return fe.name.toLowerCase(Locale.ROOT).contains("дата");
        }

        if (e instanceof AddExpr a) {
            return !isIntervalExpr(e) && (isDateLikeExpr(a.left) || isDateLikeExpr(a.right)
                    || isIntervalExpr(a.left) || isIntervalExpr(a.right));
        }
        if (e instanceof SubExpr s) {
            return !isIntervalExpr(e) && (isDateLikeExpr(s.left) || isIntervalExpr(s.right));
        }

        return false;
    }

    private String genBool(Expr e, Set<String> requiredFields) {
        if (e == null) throw new IllegalStateException("Missing boolean expression");

        if (e instanceof AndExpr ae) {
            String left = genBool(ae.left, requiredFields);
            String right = genBool(ae.right, requiredFields);

            if ("true".equals(left)) return right;
            if ("true".equals(right)) return left;
            if ("false".equals(left) || "false".equals(right)) return "false";

            return "(" + left + " && " + right + ")";
        }

        if (e instanceof OrExpr oe) {
            String left = genBool(oe.left, requiredFields);
            String right = genBool(oe.right, requiredFields);

            if ("false".equals(left)) return right;
            if ("false".equals(right)) return left;
            if ("true".equals(left) || "true".equals(right)) return "true";

            return "(" + left + " || " + right + ")";
        }

        if (e instanceof NotExpr ne) {
            String inner = genBool(ne.expr, requiredFields);

            if ("true".equals(inner)) return "false";
            if ("false".equals(inner)) return "true";

            return "(!" + inner + ")";
        }

        if (e instanceof CompareExpr
                || e instanceof IsNullExpr
                || e instanceof IsNotNullExpr
                || e instanceof LikeExpr
                || e instanceof NotLikeExpr
                || e instanceof InExpr
                || e instanceof NotInExpr
                || e instanceof BetweenExpr
                || e instanceof NotBetweenExpr) {

            return genExpr(e, requiredFields);
        }

        if (e instanceof FuncExpr f) {
            String fname = f.funcName.toLowerCase(Locale.ROOT);

            if (fname.equals("regexp_like")) {
                return genExpr(e, requiredFields);
            }
        }

        throw new IllegalStateException("Unsupported boolean expression: " + e.getClass().getSimpleName());
    }

    private String genExpr(Expr e, Set<String> requiredFields) {
        if (e == null) return "null";

        if (e instanceof FieldExpr fe) {
            requiredFields.add(fe.name);

            return "getField(data,\""
                    + escapeJava(fe.name)
                    + "\")";
        }

        if (e instanceof StringExpr se) {
            return "\"" + escapeJava(se.value) + "\"";
        }

        if (e instanceof NumberExpr ne) {
            return ne.value;
        }

        if (e instanceof IntervalExpr itv) {
            return genInterval(itv.literal);
        }

        if (e instanceof AddExpr a) {
            return "add("
                    + genExpr(a.left, requiredFields)
                    + ", "
                    + genExpr(a.right, requiredFields)
                    + ")";
        }

        if (e instanceof SubExpr s) {
            return "sub("
                    + genExpr(s.left, requiredFields)
                    + ", "
                    + genExpr(s.right, requiredFields)
                    + ")";
        }

        if (e instanceof MulExpr m) {
            return "mul("
                    + genExpr(m.left, requiredFields)
                    + ", "
                    + genExpr(m.right, requiredFields)
                    + ")";
        }

        if (e instanceof DivExpr d) {
            return "div("
                    + genExpr(d.left, requiredFields)
                    + ", "
                    + genExpr(d.right, requiredFields)
                    + ")";
        }

        if (e instanceof ModExpr m) {
            return "mod("
                    + genExpr(m.left, requiredFields)
                    + ", "
                    + genExpr(m.right, requiredFields)
                    + ")";
        }

        if (e instanceof UnaryMinusExpr u) {
            if (isIntervalExpr(u.expr)) {
                return "((Period)" + genExpr(u.expr, requiredFields) + ").negated()";
            }
            return "sub(0, "
                    + genExpr(u.expr, requiredFields)
                    + ")";
        }

        if (e instanceof CompareExpr ce) {
            String l = genExpr(ce.left, requiredFields);
            String r = genExpr(ce.right, requiredFields);

            if (isIntervalExpr(ce.left) || isIntervalExpr(ce.right)) {
                if (!isIntervalExpr(ce.left) || !isIntervalExpr(ce.right)) {
                    throw new IllegalStateException("Interval comparison requires two interval expressions");
                }
                return "compareIntervalSql(" + l + ",\"" + escapeJava(ce.op) + "\"," + r + ")";
            }

            boolean dateLike =
                    isDateLikeExpr(ce.left)
                            || isDateLikeExpr(ce.right);

            boolean numeric =
                    isNumericExpr(ce.left)
                            || isNumericExpr(ce.right)
                            || ce.op.equals(">")
                            || ce.op.equals("<")
                            || ce.op.equals(">=")
                            || ce.op.equals("<=");

            if (dateLike) {
                switch (ce.op) {
                    case "=":
                        return "dateEq(" + l + "," + r + ")";
                    case "!=":
                    case "<>":
                        return "dateNe(" + l + "," + r + ")";
                    case ">":
                        return "dateGt(" + l + "," + r + ")";
                    case "<":
                        return "dateLt(" + l + "," + r + ")";
                    case ">=":
                        return "dateGe(" + l + "," + r + ")";
                    case "<=":
                        return "dateLe(" + l + "," + r + ")";
                    default:
                        throw new IllegalStateException(
                                "Unknown op: " + ce.op);
                }
            }

            if (numeric) {
                switch (ce.op) {
                    case "=":
                        return "numEq(" + l + "," + r + ")";
                    case "!=":
                    case "<>":
                        return "numNe(" + l + "," + r + ")";
                    case ">":
                        return "numGt(" + l + "," + r + ")";
                    case "<":
                        return "numLt(" + l + "," + r + ")";
                    case ">=":
                        return "numGe(" + l + "," + r + ")";
                    case "<=":
                        return "numLe(" + l + "," + r + ")";
                    default:
                        throw new IllegalStateException(
                                "Unknown op: " + ce.op);
                }
            }

            switch (ce.op) {
                case "=":
                    return "java.util.Objects.equals("
                            + l + "," + r + ")";
                case "!=":
                case "<>":
                    return "(!java.util.Objects.equals("
                            + l + "," + r + "))";
                default:
                    throw new IllegalStateException(
                            "Unsupported op for strings: " + ce.op);
            }
        }

        if (e instanceof IsNullExpr ine) {
            return "("
                    + genExpr(ine.expr, requiredFields)
                    + "==null)";
        }

        if (e instanceof IsNotNullExpr inne) {
            return "("
                    + genExpr(inne.expr, requiredFields)
                    + "!=null)";
        }

        if (e instanceof LikeExpr le) {
            String val = genExpr(le.value, requiredFields);

            return "likeSql("
                    + val
                    + ",\""
                    + escapeJava(le.pattern)
                    + "\")";
        }

        if (e instanceof NotLikeExpr nle) {
            String val = genExpr(nle.value, requiredFields);

            return "notLikeSql("
                    + val
                    + ",\""
                    + escapeJava(nle.pattern)
                    + "\")";
        }

        if (e instanceof InExpr in) {
            StringBuilder sb = new StringBuilder();

            sb.append("inSql(")
                    .append(genExpr(in.value, requiredFields));

            for (Expr option : in.options) {
                sb.append(",")
                        .append(genExpr(option, requiredFields));
            }

            sb.append(")");
            return sb.toString();
        }

        if (e instanceof NotInExpr nin) {
            StringBuilder sb = new StringBuilder();

            sb.append("notInSql(")
                    .append(genExpr(nin.value, requiredFields));

            for (Expr option : nin.options) {
                sb.append(",")
                        .append(genExpr(option, requiredFields));
            }

            sb.append(")");
            return sb.toString();
        }

        if (e instanceof BetweenExpr be) {
            return "betweenSql("
                    + genExpr(be.value, requiredFields) + ","
                    + genExpr(be.from, requiredFields) + ","
                    + genExpr(be.to, requiredFields)
                    + ")";
        }

        if (e instanceof NotBetweenExpr nbe) {
            return "notBetweenSql("
                    + genExpr(nbe.value, requiredFields) + ","
                    + genExpr(nbe.from, requiredFields) + ","
                    + genExpr(nbe.to, requiredFields)
                    + ")";
        }

        if (e instanceof FuncExpr f) {
            String fname =
                    f.funcName.toLowerCase(Locale.ROOT);

            if (fname.equals("age")) {
                requireArity(f, 1, 2);
                String end = f.args.size() == 1 ? "current_date()" : genExpr(f.args.get(0), requiredFields);
                String start = genExpr(f.args.get(f.args.size() - 1), requiredFields);
                return "ageSql(" + end + "," + start + ")";
            }

            if (fname.equals("extract") || fname.equals("date_part")) {
                requireArity(f, 2, 2);
                String unit = extractUnit(f.args.get(0));
                validateExtractUnit(unit, isIntervalExpr(f.args.get(1)));
                return "extractSql(\"" + escapeJava(unit) + "\","
                        + genExpr(f.args.get(1), requiredFields) + ")";
            }

            if (fname.equals("substring")
                    || fname.equals("substr")) {

                if (f.args.size() == 2) {
                    return "substringSql("
                            + genExpr(f.args.get(0), requiredFields)
                            + ","
                            + genExpr(f.args.get(1), requiredFields)
                            + ",2147483647)";
                }

                if (f.args.size() == 3) {
                    return "substringSql("
                            + genExpr(f.args.get(0), requiredFields)
                            + ","
                            + genExpr(f.args.get(1), requiredFields)
                            + ","
                            + genExpr(f.args.get(2), requiredFields)
                            + ")";
                }

                return "null";
            }

            if (fname.equals("syslib.utf8_length")
                    || fname.equals("utf8_length")
                    || fname.equals("length")) {

                return "utf8Length("
                        + genExpr(f.args.get(0), requiredFields)
                        + ")";
            }

            if (fname.equals("regexp_like")) {
                return "regexp_like("
                        + genExpr(f.args.get(0), requiredFields)
                        + ","
                        + genExpr(f.args.get(1), requiredFields)
                        + ")";
            }

            if (fname.equals("upper")) {
                String x =
                        genExpr(f.args.get(0), requiredFields);

                return "(toStringSafe("
                        + x
                        + ")==null?null:toStringSafe("
                        + x
                        + ").toUpperCase(Locale.ROOT))";
            }

            if (fname.equals("years_add")) {
                return "years_add("
                        + genExpr(f.args.get(0), requiredFields)
                        + ","
                        + genExpr(f.args.get(1), requiredFields)
                        + ")";
            }

            if (fname.equals("replace")) {
                return "replace("
                        + genExpr(f.args.get(0), requiredFields)
                        + ","
                        + genExpr(f.args.get(1), requiredFields)
                        + ","
                        + genExpr(f.args.get(2), requiredFields)
                        + ")";
            }

            if (fname.equals("syslib.utf8_lower")
                    || fname.equals("utf8_lower")) {

                return "utf8_lower("
                        + genExpr(f.args.get(0), requiredFields)
                        + ")";
            }

            if (fname.equals("trim")) {
                return "trim("
                        + genExpr(f.args.get(0), requiredFields)
                        + ")";
            }

            if (fname.equals("concat")) {
                StringBuilder sb =
                        new StringBuilder("concat(");

                for (int i = 0; i < f.args.size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }

                    sb.append(
                            genExpr(
                                    f.args.get(i),
                                    requiredFields
                            )
                    );
                }

                sb.append(")");
                return sb.toString();
            }

            if (fname.equals("coalesce")) {
                StringBuilder sb =
                        new StringBuilder("coalesce(");

                for (int i = 0; i < f.args.size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }

                    sb.append(
                            genExpr(
                                    f.args.get(i),
                                    requiredFields
                            )
                    );
                }

                sb.append(")");
                return sb.toString();
            }

            if (fname.equals("current_date")) {
                requireArity(f, 0, 0);
                return "current_date()";
            }

            if (fname.equals("now")) {
                requireArity(f, 0, 0);
                return "now()";
            }

            throw new IllegalStateException("Unsupported function: " + f.funcName);
        }

        if (e instanceof CastExpr c) {
            String type =
                    c.asType == null
                            ? ""
                            : c.asType.toLowerCase(Locale.ROOT);

            String inner =
                    genExpr(c.expr, requiredFields);

            if (type.equals("int")
                    || type.equals("integer")) {

                return "toIntOrNull(" + inner + ")";
            }

            if (type.equals("bigint")
                    || type.equals("long")) {

                return "toLongOrNull(" + inner + ")";
            }

            if (type.equals("string")
                    || type.equals("varchar")
                    || type.equals("char")
                    || type.equals("text")) {

                return "castString(" + inner + ")";
            }

            if (type.equals("date")) {
                return "toDateOrNull(" + inner + ")";
            }

            return inner;
        }

        throw new IllegalStateException(
                "Unhandled expr type: " + e.getClass());
    }

    private String escapeJava(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
