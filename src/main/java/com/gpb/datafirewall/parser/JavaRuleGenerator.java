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
    public static final String GENERATOR_VERSION = "6-referenced-fields";
    private static final Logger log = LoggerFactory.getLogger(JavaRuleGenerator.class);
    private static final Pattern INTERVAL_PART = Pattern.compile(
            "\\G\\s*([+-]?\\d+)\\s+(years?|months?|weeks?|days?)\\b", Pattern.CASE_INSENSITIVE);

    /** Each generation owns its helpers; the pipeline shares this generator across threads. */
    private static final class GenerationContext extends LinkedHashSet<String> {
        private final StringBuilder decodeMethods = new StringBuilder();
        private int decodeId;
    }

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
        sb.append("    return s==null ? null : Integer.valueOf(s.codePointCount(0, s.length()));\n");
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
        sb.append("    int to = (int)Math.min((long)from + len, (long)s.length());\n");
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
        sb.append("    if(v instanceof LocalDate) return (LocalDate)v;\n");
        sb.append("    if(v instanceof LocalDateTime) return ((LocalDateTime)v).toLocalDate();\n");
        sb.append("    if(v instanceof OffsetDateTime) return ((OffsetDateTime)v).toLocalDate();\n");
        sb.append("    if(v instanceof ZonedDateTime) return ((ZonedDateTime)v).toLocalDate();\n");
        sb.append("    String s = v.toString().replace(' ', 'T');\n");
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
        appendSqlFunctionHelpers(sb);

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

        sb.append("  private Object coalesce(Object... args){\n");
        sb.append("    if(args!=null){\n");
        sb.append("      for(Object a : args){\n");
        sb.append("        if(a!=null) return a;\n");
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
        sb.append("    if(b instanceof Period){ if(a instanceof LocalDateTime) return ((LocalDateTime)a).plus((Period)b); if(a instanceof ZonedDateTime) return ((ZonedDateTime)a).plus((Period)b); LocalDate d=toDateOrNull(a); return d==null?null:d.plus((Period)b); }\n");
        sb.append("    if(a instanceof Period){ if(b instanceof LocalDateTime) return ((LocalDateTime)b).plus((Period)a); if(b instanceof ZonedDateTime) return ((ZonedDateTime)b).plus((Period)a); LocalDate d=toDateOrNull(b); return d==null?null:d.plus((Period)a); }\n");
        sb.append("    Double da=toDoubleOrNull(a), db=toDoubleOrNull(b);\n");
        sb.append("    if(da!=null && db!=null) return da+db;\n");
        sb.append("    return null;\n");
        sb.append("  }\n");

        sb.append("  private Object sub(Object a, Object b){\n");
        sb.append("    if(a instanceof Period && b instanceof Period) return ((Period)a).minus((Period)b);\n");
        sb.append("    if(b instanceof Period){ if(a instanceof LocalDateTime) return ((LocalDateTime)a).minus((Period)b); if(a instanceof ZonedDateTime) return ((ZonedDateTime)a).minus((Period)b); LocalDate d=toDateOrNull(a); return d==null?null:d.minus((Period)b); }\n");
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
        sb.append("      if(option!=null && sqlEquals(value, option)) return true;\n");
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
        sb.append("      if(sqlEquals(value, option)) return false;\n");
        sb.append("    }\n");
        sb.append("    if(hasNullOption) return false;\n");
        sb.append("    return true;\n");
        sb.append("  }\n");

        sb.append("  private boolean betweenSql(Object value, Object from, Object to){\n");
        sb.append("    if(value==null || from==null || to==null) return false;\n");
        sb.append("    if(value instanceof Period || from instanceof Period || to instanceof Period){\n");
        sb.append("      if(!(value instanceof Period) || !(from instanceof Period) || !(to instanceof Period)) return false;\n");
        sb.append("      long v=intervalDays((Period)value); return v>=intervalDays((Period)from) && v<=intervalDays((Period)to);\n");
        sb.append("    }\n");
        sb.append("    if(isTimestampObject(value) || isTimestampObject(from) || isTimestampObject(to)){\n");
        sb.append("      return timestampCompare(value,\">=\",from) && timestampCompare(value,\"<=\",to);\n");
        sb.append("    }\n");
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

        GenerationContext requiredFields = new GenerationContext();
        // Во время генерации Java-выражения одновременно собираем все FieldExpr
        String condition = genBool(expr, requiredFields);

        appendReferencedFields(sb, requiredFields);
        sb.append(requiredFields.decodeMethods);

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

    private void appendReferencedFields(StringBuilder sb, Set<String> referencedFields) {
        if (referencedFields.isEmpty()) {
            sb.append("  private static final Set<String> REFERENCED_FIELDS = Set.of();\n");
        } else {
            sb.append("  private static final Set<String> REFERENCED_FIELDS = Set.of(\n");

            int index = 0;
            for (String field : referencedFields) {
                sb.append("      \"")
                        .append(escapeJava(field))
                        .append("\"");

                if (++index < referencedFields.size()) {
                    sb.append(",");
                }
                sb.append("\n");
            }

            sb.append("  );\n");
        }

        sb.append("  @Override\n");
        sb.append("  public Set<String> referencedFields() {\n");
        sb.append("    return REFERENCED_FIELDS;\n");
        sb.append("  }\n");
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
                  private Number extractSql(String unit, Object value){
                    if(value==null) return null;
                    if(value instanceof Period){
                      Period p = (Period)value;
                      long months = p.toTotalMonths();
                      if("year".equals(unit)) return Long.valueOf(months / 12);
                      if("month".equals(unit)) return Long.valueOf(months % 12);
                      if("day".equals(unit)) return Long.valueOf(p.getDays());
                      throw new IllegalArgumentException("Unsupported EXTRACT unit for interval: " + unit);
                    }
                    if(isTimestampObject(value)){
                      LocalDateTime t = toTimestampOrNull(value);
                      if("hour".equals(unit)) return Long.valueOf(t.getHour());
                      if("minute".equals(unit)) return Long.valueOf(t.getMinute());
                      if("second".equals(unit)) return Double.valueOf(t.getSecond()+t.getNano()/1000000000d);
                      if("milliseconds".equals(unit)) return Double.valueOf(t.getSecond()*1000d+t.getNano()/1000000d);
                      if("microseconds".equals(unit)) return Double.valueOf(t.getSecond()*1000000d+t.getNano()/1000d);
                      if("epoch".equals(unit)) {
                        if(value instanceof LocalDateTime) return Double.valueOf(t.toEpochSecond(ZoneOffset.UTC)+t.getNano()/1000000000d);
                        Instant instant=timestampInstant(value);
                        return Double.valueOf(instant.getEpochSecond()+instant.getNano()/1000000000d);
                      }
                    }
                    LocalDate d = toDateOrNull(value);
                    if(d==null) return null;
                    if("epoch".equals(unit)) return Long.valueOf(d.toEpochDay()*86400L);
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
                    int cmp = Long.compare(intervalDays(a), intervalDays(b));
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

    private void appendSqlFunctionHelpers(StringBuilder sb) {
        sb.append("""
                  private boolean decodeMatch(Object value, Object search, int mode){
                    if(value==null || search==null) return value==search;
                    if(mode==1){
                      try { return new java.math.BigDecimal(value.toString().trim()).compareTo(
                          new java.math.BigDecimal(search.toString().trim()))==0; }
                      catch(NumberFormatException ex){ return false; }
                    }
                    if(mode==2) return dateEq(value,search);
                    if(mode==3) return timestampCompare(value,"=",search);
                    if(mode==4) return sqlEquals(value,search);
                    return java.util.Objects.equals(toStringSafe(value),toStringSafe(search));
                  }
                """);
        sb.append("  private Period negateInterval(Object value){\n");
        sb.append("    return value==null ? null : ((Period)value).negated();\n");
        sb.append("  }\n");
        sb.append("  private long intervalDays(Period value){\n");
        sb.append("    return value.toTotalMonths() * 30L + value.getDays();\n");
        sb.append("  }\n");
        sb.append("  private LocalDateTime toTimestampOrNull(Object value){\n");
        sb.append("    if(value==null) return null;\n");
        sb.append("    if(value instanceof LocalDateTime) return (LocalDateTime)value;\n");
        sb.append("    if(value instanceof LocalDate) return ((LocalDate)value).atStartOfDay();\n");
        sb.append("    if(value instanceof Instant) return LocalDateTime.ofInstant((Instant)value, ZoneId.systemDefault());\n");
        sb.append("    if(value instanceof OffsetDateTime) return LocalDateTime.ofInstant(((OffsetDateTime)value).toInstant(),ZoneId.systemDefault());\n");
        sb.append("    if(value instanceof ZonedDateTime) return LocalDateTime.ofInstant(((ZonedDateTime)value).toInstant(),ZoneId.systemDefault());\n");
        sb.append("    String s=value.toString().trim().replace(' ', 'T');\n");
        sb.append("    try { return LocalDateTime.parse(s); } catch(DateTimeException ex) {}\n");
        sb.append("    try { return LocalDateTime.ofInstant(OffsetDateTime.parse(s).toInstant(),ZoneId.systemDefault()); } catch(DateTimeException ex) {}\n");
        sb.append("    try { return LocalDate.parse(s).atStartOfDay(); } catch(DateTimeException ex) { return null; }\n");
        sb.append("  }\n");
        sb.append("  private ZonedDateTime timestampSql(Object value, String pattern){\n");
        sb.append("    if(value==null) return null;\n");
        sb.append("    if(pattern==null){\n");
        sb.append("      Double seconds=toDoubleOrNull(value);\n");
        sb.append("      if(seconds==null){\n");
        sb.append("        if(isTimestampObject(value)){ Instant instant=timestampInstant(value); return instant==null?null:instant.atZone(ZoneId.systemDefault()); }\n");
        sb.append("        try { return OffsetDateTime.parse(value.toString().trim().replace(' ', 'T')).toInstant().atZone(ZoneId.systemDefault()); } catch(DateTimeException ex) {}\n");
        sb.append("        LocalDateTime t=toTimestampOrNull(value); return t==null?null:t.atZone(ZoneId.systemDefault());\n");
        sb.append("      }\n");
        sb.append("      if(!Double.isFinite(seconds.doubleValue())) return null;\n");
        sb.append("      long whole=(long)Math.floor(seconds.doubleValue());\n");
        sb.append("      long nanos=Math.round((seconds.doubleValue()-whole)*1000000000d);\n");
        sb.append("      try { return Instant.ofEpochSecond(whole,nanos).atZone(ZoneId.systemDefault()); }\n");
        sb.append("      catch(DateTimeException ex){ return null; }\n");
        sb.append("    }\n");
        sb.append("    try {\n");
        sb.append("      DateTimeFormatterBuilder builder=new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern)\n");
        sb.append("        .parseDefaulting(java.time.temporal.ChronoField.YEAR,1970)\n");
        sb.append("        .parseDefaulting(java.time.temporal.ChronoField.MONTH_OF_YEAR,1)\n");
        sb.append("        .parseDefaulting(java.time.temporal.ChronoField.DAY_OF_MONTH,1)\n");
        sb.append("        .parseDefaulting(java.time.temporal.ChronoField.MINUTE_OF_HOUR,0)\n");
        sb.append("        .parseDefaulting(java.time.temporal.ChronoField.SECOND_OF_MINUTE,0);\n");
        sb.append("      if(pattern.indexOf('h')<0) builder.parseDefaulting(java.time.temporal.ChronoField.HOUR_OF_DAY,0);\n");
        sb.append("      DateTimeFormatter formatter=builder.toFormatter(Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT);\n");
        sb.append("      java.time.temporal.TemporalAccessor parsed=formatter.parse(value.toString().trim());\n");
        sb.append("      if(parsed.isSupported(java.time.temporal.ChronoField.OFFSET_SECONDS)){\n");
        sb.append("        return OffsetDateTime.from(parsed).toInstant().atZone(ZoneId.systemDefault());\n");
        sb.append("      }\n");
        sb.append("      return LocalDateTime.from(parsed).atZone(ZoneId.systemDefault());\n");
        sb.append("    } catch(DateTimeException ex){ return null; }\n");
        sb.append("  }\n");
        sb.append("  private boolean timestampCompare(Object a, String op, Object b){\n");
        sb.append("    LocalDateTime left=toTimestampOrNull(a), right=toTimestampOrNull(b);\n");
        sb.append("    if(left==null || right==null) return false;\n");
        sb.append("    int cmp;\n");
        sb.append("    if(a instanceof ZonedDateTime || a instanceof OffsetDateTime || a instanceof Instant\n");
        sb.append("        || b instanceof ZonedDateTime || b instanceof OffsetDateTime || b instanceof Instant){\n");
        sb.append("      cmp=timestampInstant(a).compareTo(timestampInstant(b));\n");
        sb.append("    } else cmp=left.compareTo(right);\n");
        sb.append("    if(\"=\".equals(op)) return cmp==0;\n");
        sb.append("    if(\"!=\".equals(op)||\"<>\".equals(op)) return cmp!=0;\n");
        sb.append("    if(\">\".equals(op)) return cmp>0;\n");
        sb.append("    if(\"<\".equals(op)) return cmp<0;\n");
        sb.append("    if(\">=\".equals(op)) return cmp>=0;\n");
        sb.append("    if(\"<=\".equals(op)) return cmp<=0;\n");
        sb.append("    throw new IllegalArgumentException(\"Unsupported timestamp comparison: \"+op);\n");
        sb.append("  }\n");
        sb.append("  private boolean isTimestampObject(Object value){\n");
        sb.append("    return value instanceof LocalDateTime || value instanceof ZonedDateTime || value instanceof OffsetDateTime || value instanceof Instant;\n");
        sb.append("  }\n");
        sb.append("  private Instant timestampInstant(Object value){\n");
        sb.append("    if(value instanceof Instant) return (Instant)value;\n");
        sb.append("    if(value instanceof ZonedDateTime) return ((ZonedDateTime)value).toInstant();\n");
        sb.append("    if(value instanceof OffsetDateTime) return ((OffsetDateTime)value).toInstant();\n");
        sb.append("    LocalDateTime t=toTimestampOrNull(value);\n");
        sb.append("    return t==null?null:t.atZone(ZoneId.systemDefault()).toInstant();\n");
        sb.append("  }\n");
        sb.append("  private boolean sqlEquals(Object left, Object right){\n");
        sb.append("    if(left instanceof Period || right instanceof Period){\n");
        sb.append("      return left instanceof Period && right instanceof Period\n");
        sb.append("        && intervalDays((Period)left)==intervalDays((Period)right);\n");
        sb.append("    }\n");
        sb.append("    if(isTimestampObject(left) || isTimestampObject(right)){\n");
        sb.append("      return timestampCompare(left,\"=\",right);\n");
        sb.append("    }\n");
        sb.append("    if(left instanceof LocalDate || right instanceof LocalDate){\n");
        sb.append("      return dateEq(left,right);\n");
        sb.append("    }\n");
        sb.append("    return java.util.Objects.equals(left,right);\n");
        sb.append("  }\n");
        sb.append("  private String utf8Substring(Object src, Object startObj, Object lenObj){\n");
        sb.append("    String s=toStringSafe(src);\n");
        sb.append("    Integer start=toIntOrNull(startObj), len=toIntOrNull(lenObj);\n");
        sb.append("    if(s==null || start==null || len==null) return null;\n");
        sb.append("    if(len.intValue()<=0) return \"\";\n");
        sb.append("    int count=s.codePointCount(0,s.length());\n");
        sb.append("    long from=start.intValue()>0 ? (long)start.intValue()-1 : start.intValue()<0 ? (long)count+start.intValue() : 0;\n");
        sb.append("    if(from<0 || from>=count) return \"\";\n");
        sb.append("    int to=(int)Math.min(from+len.intValue(),(long)count);\n");
        sb.append("    return s.substring(s.offsetByCodePoints(0,(int)from),s.offsetByCodePoints(0,to));\n");
        sb.append("  }\n");
        sb.append("  private String btrimSql(Object value, Object characters){\n");
        sb.append("    String s=toStringSafe(value), chars=toStringSafe(characters);\n");
        sb.append("    if(s==null || chars==null) return null;\n");
        sb.append("    int from=0, to=s.length();\n");
        sb.append("    while(from<to){\n");
        sb.append("      int cp=s.codePointAt(from);\n");
        sb.append("      if(chars.indexOf(cp)<0) break;\n");
        sb.append("      from+=Character.charCount(cp);\n");
        sb.append("    }\n");
        sb.append("    while(to>from){\n");
        sb.append("      int cp=s.codePointBefore(to);\n");
        sb.append("      if(chars.indexOf(cp)<0) break;\n");
        sb.append("      to-=Character.charCount(cp);\n");
        sb.append("    }\n");
        sb.append("    return s.substring(from,to);\n");
        sb.append("  }\n");
        sb.append("  private Double absSql(Object value){\n");
        sb.append("    Double n=toDoubleOrNull(value);\n");
        sb.append("    return n==null ? null : Double.valueOf(Math.abs(n.doubleValue()));\n");
        sb.append("  }\n");
        sb.append("  private String postgresRegex(String pattern){\n");
        sb.append("    String[] classes={\"alnum\",\"alpha\",\"ascii\",\"blank\",\"cntrl\",\"digit\",\"graph\",\"lower\",\"print\",\"punct\",\"space\",\"upper\",\"xdigit\"};\n");
        sb.append("    String[] names={\"Alnum\",\"Alpha\",\"ASCII\",\"Blank\",\"Cntrl\",\"Digit\",\"Graph\",\"Lower\",\"Print\",\"Punct\",\"Space\",\"Upper\",\"XDigit\"};\n");
        sb.append("    String out=pattern;\n");
        sb.append("    for(int i=0;i<classes.length;i++) out=out.replace(\"[:\"+classes[i]+\":]\",\"\\\\p{\"+names[i]+\"}\");\n");
        sb.append("    out=out.replace(\"\\\\m\",\"(?<!\\\\w)(?=\\\\w)\").replace(\"\\\\M\",\"(?<=\\\\w)(?!\\\\w)\");\n");
        sb.append("    return out.replace(\"\\\\y\",\"\\\\b\").replace(\"\\\\Y\",\"\\\\B\");\n");
        sb.append("  }\n");
        sb.append("  private String regexReplacement(java.util.regex.Matcher match, String replacement){\n");
        sb.append("    StringBuilder out=new StringBuilder();\n");
        sb.append("    for(int i=0;i<replacement.length();i++){\n");
        sb.append("      char c=replacement.charAt(i);\n");
        sb.append("      if(c=='\\\\' && i+1<replacement.length()){\n");
        sb.append("        char next=replacement.charAt(++i);\n");
        sb.append("        if(next>='1' && next<='9'){\n");
        sb.append("          int group=next-'0';\n");
        sb.append("          String value=group<=match.groupCount() ? match.group(group) : null;\n");
        sb.append("          if(value!=null) out.append(value);\n");
        sb.append("        } else if(next=='&') out.append(match.group());\n");
        sb.append("        else if(next=='\\\\') out.append('\\\\');\n");
        sb.append("        else out.append('\\\\').append(next);\n");
        sb.append("      } else out.append(c);\n");
        sb.append("    }\n");
        sb.append("    return out.toString();\n");
        sb.append("  }\n");
        sb.append("  private String regexpReplaceSql(Object value, Object patternValue, Object replacementValue, Object flagsValue){\n");
        sb.append("    String s=toStringSafe(value), regex=toStringSafe(patternValue), replacement=toStringSafe(replacementValue), flags=toStringSafe(flagsValue);\n");
        sb.append("    if(s==null || regex==null || replacement==null || flags==null) return null;\n");
        sb.append("    int options=Pattern.UNICODE_CHARACTER_CLASS|Pattern.DOTALL;\n");
        sb.append("    boolean global=false, quoted=false;\n");
        sb.append("    for(int i=0;i<flags.length();i++){\n");
        sb.append("      char flag=flags.charAt(i);\n");
        sb.append("      if(flag=='g') global=true;\n");
        sb.append("      else if(flag=='i') options|=Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE;\n");
        sb.append("      else if(flag=='c') options&=~Pattern.CASE_INSENSITIVE;\n");
        sb.append("      else if(flag=='s') { options|=Pattern.DOTALL; options&=~Pattern.MULTILINE; }\n");
        sb.append("      else if(flag=='n'||flag=='m') { options&=~Pattern.DOTALL; options|=Pattern.MULTILINE; }\n");
        sb.append("      else if(flag=='p') { options&=~Pattern.DOTALL; options&=~Pattern.MULTILINE; }\n");
        sb.append("      else if(flag=='w') options|=Pattern.DOTALL|Pattern.MULTILINE;\n");
        sb.append("      else if(flag=='x') options|=Pattern.COMMENTS;\n");
        sb.append("      else if(flag=='t') options&=~Pattern.COMMENTS;\n");
        sb.append("      else if(flag=='q') quoted=true;\n");
        sb.append("      else throw new IllegalArgumentException(\"Unsupported REGEXP_REPLACE flag: \"+flag);\n");
        sb.append("    }\n");
        sb.append("    if(quoted) options&=~Pattern.COMMENTS;\n");
        sb.append("    java.util.regex.Matcher matcher=Pattern.compile(quoted ? Pattern.quote(regex) : postgresRegex(regex),options).matcher(s);\n");
        sb.append("    StringBuilder out=new StringBuilder();\n");
        sb.append("    int end=0;\n");
        sb.append("    while(matcher.find()){\n");
        sb.append("      out.append(s.substring(end,matcher.start())).append(regexReplacement(matcher,replacement));\n");
        sb.append("      end=matcher.end();\n");
        sb.append("      if(!global) break;\n");
        sb.append("    }\n");
        sb.append("    return out.append(s.substring(end)).toString();\n");
        sb.append("  }\n");
    }

    private String functionName(FuncExpr f) {
        String name = f.funcName.toLowerCase(Locale.ROOT);
        if (name.startsWith("pg_catalog.")) return name.substring("pg_catalog.".length());
        if (name.startsWith("syslib.utf8_")) return name.substring("syslib.".length());
        return name;
    }

    private boolean isTimestampExpr(Expr e) {
        if (e instanceof CaseExpr c) {
            return !isIntervalExpr(e) && caseResults(c).stream().anyMatch(this::isTimestampExpr);
        }
        if (e instanceof CastExpr c) {
            return "timestamp".equalsIgnoreCase(c.asType) || "datetime".equalsIgnoreCase(c.asType);
        }
        if (e instanceof FuncExpr f) {
            String name = functionName(f);
            return name.equals("to_timestamp")
                    || (name.equals("coalesce") && !isIntervalExpr(e) && f.args.stream().anyMatch(this::isTimestampExpr))
                    || (name.equals("decode") && decodeResults(f).stream().anyMatch(this::isTimestampExpr));
        }
        if (e instanceof AddExpr a) return !isIntervalExpr(e) && (isTimestampExpr(a.left) || isTimestampExpr(a.right));
        if (e instanceof SubExpr sub) return !isIntervalExpr(e) && isTimestampExpr(sub.left);
        return false;
    }

    private void validateRegexpFlags(String flags) {
        for (int i = 0; i < flags.length(); i++) {
            if ("gicsnmpwxtq".indexOf(flags.charAt(i)) < 0) {
                throw new IllegalStateException("Unsupported REGEXP_REPLACE flag: " + flags.charAt(i));
            }
        }
    }

    /** Supported PostgreSQL format tokens; never pass a SQL mask directly to Java. */
    private String timestampPattern(String mask) {
        String[] sql = {"HH24", "HH12", "MONTH", "YYYY", "MON", "HH", "MI", "SS", "MS", "US", "MM", "DD", "AM", "PM", "OF"};
        String[] patterns = {"HH", "hh", "MMMM", "uuuu", "MMM", "hh", "mm", "ss", "SSS", "SSSSSS", "MM", "dd", "a", "a", "xxx"};
        StringBuilder out = new StringBuilder();
        StringBuilder literal = new StringBuilder();
        for (int i = 0; i < mask.length();) {
            if (mask.charAt(i) == '"') {
                int end = mask.indexOf('"', i + 1);
                if (end < 0) throw new IllegalStateException("Unclosed literal in timestamp format: " + mask);
                literal.append(mask, i + 1, end);
                i = end + 1;
                continue;
            }
            if (mask.regionMatches(true, i, "FX", 0, 2)) { i += 2; continue; }
            if (mask.regionMatches(true, i, "TZH:TZM", 0, 7)) { appendTimestampLiteral(out, literal); out.append("xxx"); i += 7; continue; }
            if (mask.regionMatches(true, i, "FF", 0, 2) && i + 2 < mask.length()
                    && mask.charAt(i + 2) >= '1' && mask.charAt(i + 2) <= '9') {
                appendTimestampLiteral(out, literal);
                out.append("S".repeat(mask.charAt(i + 2) - '0'));
                i += 3;
                continue;
            }
            boolean matched = false;
            for (int token = 0; token < sql.length; token++) {
                if (mask.regionMatches(true, i, sql[token], 0, sql[token].length())) {
                    appendTimestampLiteral(out, literal);
                    out.append(patterns[token]); i += sql[token].length(); matched = true; break;
                }
            }
            if (matched) continue;
            char c = mask.charAt(i++);
            if (Character.isLetter(c)) {
                throw new IllegalStateException("Unsupported timestamp format token in: " + mask);
            }
            literal.append(c);
        }
        appendTimestampLiteral(out, literal);
        String pattern = out.toString();
        try { java.time.format.DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH); }
        catch (IllegalArgumentException ex) { throw new IllegalStateException("Invalid timestamp format: " + mask, ex); }
        return pattern;
    }

    private void appendTimestampLiteral(StringBuilder out, StringBuilder literal) {
        if (literal.length() == 0) return;
        out.append('\'').append(literal.toString().replace("'", "''")).append('\'');
        literal.setLength(0);
    }

    private boolean isIntervalExpr(Expr e) {
        if (e instanceof IntervalExpr) return true;
        if (e instanceof CaseExpr c) return caseResults(c).stream().anyMatch(this::isIntervalExpr);
        if (e instanceof CastExpr c) return "interval".equalsIgnoreCase(c.asType);
        if (e instanceof FuncExpr f) {
            String fn = functionName(f);
            if (fn.equals("age")) return true;
            if (fn.equals("coalesce")) return f.args.stream().anyMatch(this::isIntervalExpr);
            if (fn.equals("decode")) return decodeResults(f).stream().anyMatch(this::isIntervalExpr);
        }
        if (e instanceof AddExpr a) return isIntervalExpr(a.left) && isIntervalExpr(a.right);
        if (e instanceof SubExpr s) return isIntervalExpr(s.left) && isIntervalExpr(s.right);
        if (e instanceof UnaryMinusExpr u) return isIntervalExpr(u.expr);
        return false;
    }

    /** Only THEN/ELSE expressions determine CASE's return type; WHEN is boolean. */
    private java.util.List<Expr> caseResults(CaseExpr c) {
        java.util.List<Expr> results = new java.util.ArrayList<>();
        for (CaseExpr.Branch branch : c.branches) results.add(branch.result);
        results.add(c.elseExpr);
        return results;
    }

    /** Only result/default expressions determine DECODE's return type. */
    private java.util.List<Expr> decodeResults(FuncExpr f) {
        requireDecodeArity(f);
        java.util.List<Expr> results = new java.util.ArrayList<>();
        for (int i = 2; i < f.args.size(); i += 2) results.add(f.args.get(i));
        if (f.args.size() % 2 == 0) results.add(f.args.get(f.args.size() - 1));
        return results;
    }

    private void requireDecodeArity(FuncExpr f) {
        if (f.args.size() == 2) {
            throw new IllegalStateException("Conditional DECODE requires at least 3 arguments: "
                    + "value, search, result [, search, result ...] [, default]. "
                    + "The PostgreSQL binary DECODE(text, format) overload is not supported.");
        }
        requireArity(f, 3, 1000);
    }

    private int decodeComparisonMode(FuncExpr f) {
        java.util.List<Expr> inputs = new java.util.ArrayList<>();
        inputs.add(f.args.get(0));
        for (int i = 1; i + 1 < f.args.size(); i += 2) inputs.add(f.args.get(i));
        if (inputs.stream().anyMatch(this::isIntervalExpr)) return 4;
        if (inputs.stream().anyMatch(this::isTimestampExpr)) return 3;
        if (inputs.stream().anyMatch(this::isDateLikeExpr)) return 2;
        if (inputs.stream().anyMatch(this::isNumericExpr)) return 1;
        return 0;
    }

    private String genDecode(FuncExpr f, Set<String> requiredFields) {
        requireDecodeArity(f);
        GenerationContext context = (GenerationContext) requiredFields;
        String name = "decodeValue" + context.decodeId++;
        int mode = decodeComparisonMode(f);
        StringBuilder method = new StringBuilder("  private Object ")
                .append(name).append("(java.util.Map<String,String> data){\n")
                .append("    Object value=").append(genExpr(f.args.get(0), requiredFields)).append(";\n");
        // Java if/return keeps search and result evaluation lazy; no lambda/inner class bytecode.
        for (int i = 1; i + 1 < f.args.size(); i += 2) {
            String search = genExpr(f.args.get(i), requiredFields);
            String result = genExpr(f.args.get(i + 1), requiredFields);
            method.append("    if(decodeMatch(value,").append(search).append(",").append(mode)
                    .append(")) return ").append(result).append(";\n");
        }
        String fallback = f.args.size() % 2 == 0
                ? genExpr(f.args.get(f.args.size() - 1), requiredFields) : "null";
        method.append("    return ").append(fallback).append(";\n  }\n");
        context.decodeMethods.append(method);
        return name + "(data)";
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

    private void validateExtractUnit(String unit, Expr source) {
        boolean interval = isIntervalExpr(source);
        if (unit.equals("year") || unit.equals("month") || unit.equals("day")) return;
        if (!interval && (unit.equals("quarter") || unit.equals("dow") || unit.equals("isodow")
                || unit.equals("doy") || unit.equals("week") || unit.equals("isoyear") || unit.equals("epoch"))) return;
        if (isTimestampExpr(source) && (unit.equals("hour") || unit.equals("minute") || unit.equals("second")
                || unit.equals("milliseconds") || unit.equals("microseconds"))) return;
        throw new IllegalStateException("Unsupported EXTRACT/DATE_PART unit for "
                + (interval ? "interval: " : "date/timestamp: ") + unit);
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
        if (e instanceof CaseExpr c) return caseResults(c).stream().anyMatch(this::isNumericExpr);

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
            String fn = functionName(f);
            return fn.equals("syslib.utf8_length")
                    || fn.equals("utf8_length")
                    || fn.equals("length")
                    || fn.equals("extract")
                    || fn.equals("date_part")
                    || fn.equals("abs")
                    || (fn.equals("coalesce") && f.args.stream().anyMatch(this::isNumericExpr))
                    || (fn.equals("decode") && decodeResults(f).stream().anyMatch(this::isNumericExpr));
        }

        return (e instanceof AddExpr)
                || (e instanceof SubExpr)
                || (e instanceof MulExpr)
                || (e instanceof DivExpr)
                || (e instanceof ModExpr)
                || (e instanceof UnaryMinusExpr);
    }

    private boolean isDateLikeExpr(Expr e) {
        if (e instanceof CaseExpr c) {
            return !isIntervalExpr(e) && caseResults(c).stream().anyMatch(this::isDateLikeExpr);
        }
        if (e instanceof FuncExpr f) {
            String fn = functionName(f);
            if (fn.equals("current_date") || fn.equals("now") || fn.equals("years_add") || fn.equals("to_date")) return true;
            if (fn.equals("coalesce")) return !isIntervalExpr(e) && f.args.stream().anyMatch(this::isDateLikeExpr);
            if (fn.equals("decode")) return decodeResults(f).stream().anyMatch(this::isDateLikeExpr);
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
            String fname = functionName(f);

            if (fname.equals("regexp_like")) {
                requireArity(f, 2, 2);
                return genExpr(e, requiredFields);
            }
            throw new IllegalStateException("Function " + f.funcName
                    + " cannot be used as a boolean predicate; use a comparison or IS NULL");
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

        if (e instanceof NullExpr) return "null";

        if (e instanceof NumberExpr ne) {
            return ne.value;
        }

        if (e instanceof IntervalExpr itv) {
            return genInterval(itv.literal);
        }

        if (e instanceof CaseExpr c) {
            return genCase(c, requiredFields);
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
                return "negateInterval(" + genExpr(u.expr, requiredFields) + ")";
            }
            return "sub(0, "
                    + genExpr(u.expr, requiredFields)
                    + ")";
        }

        if (e instanceof CompareExpr ce) {
            String l = genExpr(ce.left, requiredFields);
            String r = genExpr(ce.right, requiredFields);

            if (ce.left instanceof NullExpr || ce.right instanceof NullExpr) return "false";

            if (isIntervalExpr(ce.left) || isIntervalExpr(ce.right)) {
                if (!isIntervalExpr(ce.left) || !isIntervalExpr(ce.right)) {
                    throw new IllegalStateException("Interval comparison requires two interval expressions. "
                            + "For numeric age use EXTRACT(year FROM age(...)); for an interval boundary use INTERVAL '18 years'.");
                }
                return "compareIntervalSql(" + l + ",\"" + escapeJava(ce.op) + "\"," + r + ")";
            }

            if (isTimestampExpr(ce.left) || isTimestampExpr(ce.right)) {
                return "timestampCompare(" + l + ",\"" + escapeJava(ce.op) + "\"," + r + ")";
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
            String fname = functionName(f);

            if (fname.equals("age")) {
                requireArity(f, 1, 2);
                String end = f.args.size() == 1 ? "current_date()" : genExpr(f.args.get(0), requiredFields);
                String start = genExpr(f.args.get(f.args.size() - 1), requiredFields);
                return "ageSql(" + end + "," + start + ")";
            }

            if (fname.equals("extract") || fname.equals("date_part")) {
                requireArity(f, 2, 2);
                String unit = extractUnit(f.args.get(0));
                validateExtractUnit(unit, f.args.get(1));
                return "extractSql(\"" + escapeJava(unit) + "\","
                        + genExpr(f.args.get(1), requiredFields) + ")";
            }

            if (fname.equals("to_timestamp") || fname.equals("to_date")) {
                requireArity(f, fname.equals("to_date") ? 2 : 1, 2);
                String pattern = "null";
                if (f.args.size() == 2) {
                    if (!(f.args.get(1) instanceof StringExpr mask)) {
                        throw new IllegalStateException("Function " + f.funcName + " format must be a constant string");
                    }
                    pattern = "\"" + escapeJava(timestampPattern(mask.value)) + "\"";
                }
                String call = "timestampSql(" + genExpr(f.args.get(0), requiredFields) + "," + pattern + ")";
                return fname.equals("to_date") ? "toDateOrNull(" + call + ")" : call;
            }
            if (fname.equals("regexp_replace")) {
                requireArity(f, 3, 4);
                if (f.args.size() == 4 && f.args.get(3) instanceof StringExpr flags) {
                    validateRegexpFlags(flags.value);
                }
                return "regexpReplaceSql(" + genExpr(f.args.get(0), requiredFields) + ","
                        + genExpr(f.args.get(1), requiredFields) + "," + genExpr(f.args.get(2), requiredFields)
                        + "," + (f.args.size() == 4 ? genExpr(f.args.get(3), requiredFields) : "\"\"") + ")";
            }
            if (fname.equals("btrim")) {
                requireArity(f, 1, 2);
                return "btrimSql(" + genExpr(f.args.get(0), requiredFields) + ","
                        + (f.args.size() == 2 ? genExpr(f.args.get(1), requiredFields) : "\" \"") + ")";
            }
            if (fname.equals("abs")) {
                requireArity(f, 1, 1);
                return "absSql(" + genExpr(f.args.get(0), requiredFields) + ")";
            }
            if (fname.equals("utf8_substr")) {
                requireArity(f, 2, 3);
                return "utf8Substring(" + genExpr(f.args.get(0), requiredFields) + ","
                        + genExpr(f.args.get(1), requiredFields) + ","
                        + (f.args.size() == 3 ? genExpr(f.args.get(2), requiredFields) : "2147483647") + ")";
            }

            if (fname.equals("substring")
                    || fname.equals("substr")) {
                requireArity(f, 2, 3);

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
                requireArity(f, 1, 1);

                return "utf8Length("
                        + genExpr(f.args.get(0), requiredFields)
                        + ")";
            }

            if (fname.equals("regexp_like")) {
                requireArity(f, 2, 2);
                return "regexp_like("
                        + genExpr(f.args.get(0), requiredFields)
                        + ","
                        + genExpr(f.args.get(1), requiredFields)
                        + ")";
            }

            if (fname.equals("upper")) {
                requireArity(f, 1, 1);
                String x =
                        genExpr(f.args.get(0), requiredFields);

                return "(toStringSafe("
                        + x
                        + ")==null?null:toStringSafe("
                        + x
                        + ").toUpperCase(Locale.ROOT))";
            }

            if (fname.equals("years_add")) {
                requireArity(f, 2, 2);
                return "years_add("
                        + genExpr(f.args.get(0), requiredFields)
                        + ","
                        + genExpr(f.args.get(1), requiredFields)
                        + ")";
            }

            if (fname.equals("replace")) {
                requireArity(f, 3, 3);
                return "replace("
                        + genExpr(f.args.get(0), requiredFields)
                        + ","
                        + genExpr(f.args.get(1), requiredFields)
                        + ","
                        + genExpr(f.args.get(2), requiredFields)
                        + ")";
            }

            if (fname.equals("syslib.utf8_lower")
                    || fname.equals("utf8_lower") || fname.equals("lower")) {
                requireArity(f, 1, 1);

                return "utf8_lower("
                        + genExpr(f.args.get(0), requiredFields)
                        + ")";
            }

            if (fname.equals("trim")) {
                requireArity(f, 1, 1);
                return "trim("
                        + genExpr(f.args.get(0), requiredFields)
                        + ")";
            }

            if (fname.equals("concat")) {
                requireArity(f, 1, 1000);
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

            if (fname.equals("decode")) {
                return genDecode(f, requiredFields);
            }

            if (fname.equals("coalesce")) {
                requireArity(f, 1, 1000);
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

            if (type.equals("timestamp") || type.equals("datetime")) {
                return "toTimestampOrNull(" + inner + ")";
            }
            if (type.equals("interval")) {
                if (!isIntervalExpr(c.expr)) throw new IllegalStateException("CAST AS interval requires an interval expression");
                return inner;
            }
            if (type.equals("date")) {
                return "toDateOrNull(" + inner + ")";
            }

            return inner;
        }

        throw new IllegalStateException(
                "Unhandled expr type: " + e.getClass());
    }

    private String genCase(CaseExpr c, Set<String> requiredFields) {
        java.util.List<String> conditions = new java.util.ArrayList<>();
        java.util.List<String> results = new java.util.ArrayList<>();

        // Generate in SQL order so requiredFields keeps its deterministic encounter order.
        for (CaseExpr.Branch branch : c.branches) {
            conditions.add(genBool(branch.condition, requiredFields));
            results.add(genExpr(branch.result, requiredFields));
        }

        String generated = "(Object)(" + genExpr(c.elseExpr, requiredFields) + ")";
        for (int i = conditions.size() - 1; i >= 0; i--) {
            // Object casts prevent Java/Janino from choosing an incompatible primitive
            // common type for heterogeneous SQL results. Conditional evaluation stays lazy.
            generated = "(" + conditions.get(i) + " ? (Object)(" + results.get(i)
                    + ") : " + generated + ")";
        }
        return generated;
    }

    private String escapeJava(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
