package com.gpb.datafirewall.parser;

import com.gpb.datafirewall.model.Rule;
import com.gpb.datafirewall.parser.ast.*;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Генератор Java Source (код Java в String) на основе AST
 *
 * Важно: Janino НЕ поддерживает switch-expression, поэтому используем if/else.
 */
public class JavaRuleGenerator {

    public String generate(String className, Expr expr) {
        StringBuilder sb = new StringBuilder();

        sb.append("import java.util.*;\n");
        sb.append("import java.util.regex.*;\n");
        sb.append("import java.time.*;\n");
        sb.append("import java.time.format.*;\n");
        sb.append("import com.gpb.datafirewall.model.Rule;\n");

        sb.append("public class ").append(className).append(" implements Rule {\n");

        sb.append("  private String toStringSafe(Object o){ return o==null?null:o.toString(); }\n");

        sb.append("  private Double toDoubleOrNull(Object v){\n");
        sb.append("    try { return v==null?null:Double.valueOf(v.toString()); }\n");
        sb.append("    catch(Exception e){ return null; }\n");
        sb.append("  }\n");

        sb.append("  private Integer utf8Length(String s){\n");
        sb.append("    return s==null ? null : Integer.valueOf(s.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);\n");
        sb.append("  }\n");

        sb.append("  private boolean regexp_like(String s, String pat){\n");
        sb.append("    if(s==null) return false;\n");
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
        sb.append("    if(a instanceof LocalDate && b instanceof Period) return ((LocalDate)a).plus((Period)b);\n");
        sb.append("    if(a instanceof Period && b instanceof LocalDate) return ((LocalDate)b).plus((Period)a);\n");
        sb.append("    Double da=toDoubleOrNull(a), db=toDoubleOrNull(b);\n");
        sb.append("    if(da!=null && db!=null) return da+db;\n");
        sb.append("    return null;\n");
        sb.append("  }\n");

        sb.append("  private Object sub(Object a, Object b){\n");
        sb.append("    if(a instanceof LocalDate && b instanceof Period) return ((LocalDate)a).minus((Period)b);\n");
        sb.append("    Double da=toDoubleOrNull(a), db=toDoubleOrNull(b);\n");
        sb.append("    if(da!=null && db!=null) return da-db;\n");
        sb.append("    return null;\n");
        sb.append("  }\n");

        sb.append("  private Object mul(Object a, Object b){ Double da=toDoubleOrNull(a), db=toDoubleOrNull(b); if(da!=null && db!=null) return da*db; return null; }\n");
        sb.append("  private Object div(Object a, Object b){ Double da=toDoubleOrNull(a), db=toDoubleOrNull(b); if(da!=null && db!=null && db!=0d) return da/db; return null; }\n");
        sb.append("  private Object mod(Object a, Object b){ Double da=toDoubleOrNull(a), db=toDoubleOrNull(b); if(da!=null && db!=null && db!=0d) return da%db; return null; }\n");

        sb.append("  private Period intervalToPeriod(String lit){\n");
        sb.append("    if(lit==null) return null;\n");
        sb.append("    String s = lit.trim().toLowerCase(Locale.ROOT);\n");
        sb.append("    if(s.isEmpty()) return null;\n");
        sb.append("    String[] parts = s.split(\"\\\\s+\");\n");
        sb.append("    if(parts.length < 2) return null;\n");
        sb.append("    int n; try{ n=Integer.parseInt(parts[0]); } catch(Exception e){ return null; }\n");
        sb.append("    String unit = parts[1];\n");
        sb.append("    if(unit.endsWith(\"s\")) unit = unit.substring(0, unit.length()-1);\n");
        sb.append("    if(\"day\".equals(unit)) return Period.ofDays(n);\n");
        sb.append("    if(\"month\".equals(unit)) return Period.ofMonths(n);\n");
        sb.append("    if(\"year\".equals(unit)) return Period.ofYears(n);\n");
        sb.append("    return null;\n");
        sb.append("  }\n");

        sb.append("  public boolean apply(java.util.Map<String,String> data){\n");
        sb.append("    try{\n");
        sb.append("      return ").append(genBool(expr)).append(";\n");
        sb.append("    } catch(Exception e){ return false; }\n");
        sb.append("  }\n");

        sb.append("}\n");
        return sb.toString();
    }

    private boolean isNumericExpr(Expr e) {
        if (e instanceof NumberExpr) return true;
        if (e instanceof FuncExpr f) {
            String fn = f.funcName.toLowerCase(Locale.ROOT);
            return fn.equals("syslib.utf8_length") || fn.equals("utf8_length") || fn.equals("length")
                    || fn.equals("extract") || fn.equals("age");
        }
        return (e instanceof AddExpr) || (e instanceof SubExpr) || (e instanceof MulExpr)
                || (e instanceof DivExpr) || (e instanceof ModExpr) || (e instanceof UnaryMinusExpr);
    }

    private boolean isDateLikeExpr(Expr e) {
        if (e instanceof FuncExpr f) {
            String fn = f.funcName.toLowerCase(Locale.ROOT);
            if (fn.equals("current_date") || fn.equals("now")) return true;
        }
        if (e instanceof CastExpr) return true;
        if (e instanceof FieldExpr fe) return fe.name.toLowerCase(Locale.ROOT).contains("дата");
        if (e instanceof AddExpr a) return isDateLikeExpr(a.left) || isDateLikeExpr(a.right);
        if (e instanceof SubExpr s) return isDateLikeExpr(s.left) || isDateLikeExpr(s.right);
        return false;
    }

    private String genBool(Expr e) {
        if (e == null) return "false";

        if (e instanceof AndExpr || e instanceof OrExpr || e instanceof NotExpr
                || e instanceof CompareExpr || e instanceof IsNullExpr || e instanceof IsNotNullExpr
                || e instanceof LikeExpr || e instanceof InExpr) {
            return genExpr(e);
        }

        return "false";
    }

    private String genExpr(Expr e) {
        if (e == null) return "null";

        if (e instanceof FieldExpr fe) {
            return "toStringSafe(data.get(\"" + escapeJava(fe.name) + "\"))";
        }
        if (e instanceof StringExpr se) {
            return "\"" + escapeJava(se.value) + "\"";
        }
        if (e instanceof NumberExpr ne) {
            return ne.value;
        }

        if (e instanceof IntervalExpr itv) {
            return "intervalToPeriod(\"" + escapeJava(itv.literal) + "\")";
        }
        if (e instanceof AddExpr a) return "add(" + genExpr(a.left) + ", " + genExpr(a.right) + ")";
        if (e instanceof SubExpr s) return "sub(" + genExpr(s.left) + ", " + genExpr(s.right) + ")";
        if (e instanceof MulExpr m) return "mul(" + genExpr(m.left) + ", " + genExpr(m.right) + ")";
        if (e instanceof DivExpr d) return "div(" + genExpr(d.left) + ", " + genExpr(d.right) + ")";
        if (e instanceof ModExpr m) return "mod(" + genExpr(m.left) + ", " + genExpr(m.right) + ")";
        if (e instanceof UnaryMinusExpr u) return "sub(0, " + genExpr(u.expr) + ")";

        if (e instanceof CompareExpr ce) {
            String l = genExpr(ce.left);
            String r = genExpr(ce.right);

            boolean dateLike = isDateLikeExpr(ce.left) || isDateLikeExpr(ce.right);
            boolean numeric = isNumericExpr(ce.left) || isNumericExpr(ce.right)
                    || ce.op.equals(">") || ce.op.equals("<") || ce.op.equals(">=") || ce.op.equals("<=");

            if (dateLike) {
                switch (ce.op) {
                    case "=":  return "dateEq(" + l + "," + r + ")";
                    case "!=":
                    case "<>": return "dateNe(" + l + "," + r + ")";
                    case ">":  return "dateGt(" + l + "," + r + ")";
                    case "<":  return "dateLt(" + l + "," + r + ")";
                    case ">=": return "dateGe(" + l + "," + r + ")";
                    case "<=": return "dateLe(" + l + "," + r + ")";
                    default: throw new IllegalStateException("Unknown op: " + ce.op);
                }
            }

            if (numeric) {
                switch (ce.op) {
                    case "=":  return "numEq(" + l + "," + r + ")";
                    case "!=":
                    case "<>": return "numNe(" + l + "," + r + ")";
                    case ">":  return "numGt(" + l + "," + r + ")";
                    case "<":  return "numLt(" + l + "," + r + ")";
                    case ">=": return "numGe(" + l + "," + r + ")";
                    case "<=": return "numLe(" + l + "," + r + ")";
                    default: throw new IllegalStateException("Unknown op: " + ce.op);
                }
            }

            // string compare only for = and !=
            switch (ce.op) {
                case "=":
                    return "(" + l + "!=null && " + l + ".equals(" + r + "))";
                case "!=":
                case "<>":
                    return "(" + l + "==null || !" + l + ".equals(" + r + "))";
                default:
                    throw new IllegalStateException("Unsupported op for strings: " + ce.op);
            }
        }

        // null checks
        if (e instanceof IsNullExpr ine) return "(" + genExpr(ine.expr) + "==null)";
        if (e instanceof IsNotNullExpr inne) return "(" + genExpr(inne.expr) + "!=null)";

        // like
        if (e instanceof LikeExpr le) {
            String val = genExpr(le.value);
            String regex = escapeJava(le.pattern).replace("%", ".*").replace("_", ".");
            return "(toStringSafe(" + val + ")!=null && toStringSafe(" + val + ").matches(\"" + regex + "\"))";
        }

        // in
        if (e instanceof InExpr in) {
            String val = "toStringSafe(" + genExpr(in.value) + ")";
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < in.options.size(); i++) {
                if (i > 0) sb.append(" || ");
                sb.append(val).append("!=null && ").append(val).append(".equals(")
                        .append(genExpr(in.options.get(i))).append(")");
            }
            if (in.options.isEmpty()) sb.append("false");
            sb.append(")");
            return sb.toString();
        }

        if (e instanceof AndExpr ae) return "(" + genBool(ae.left) + " && " + genBool(ae.right) + ")";
        if (e instanceof OrExpr oe)  return "(" + genBool(oe.left) + " || " + genBool(oe.right) + ")";
        if (e instanceof NotExpr ne) return "(!" + genBool(ne.expr) + ")";

        // functions
        if (e instanceof FuncExpr f) {
            String fname = f.funcName.toLowerCase(Locale.ROOT);

            if (fname.equals("syslib.utf8_length") || fname.equals("utf8_length") || fname.equals("length")) {
                return "utf8Length(" + genExpr(f.args.get(0)) + ")";
            }
            if (fname.equals("regexp_like")) {
                return "regexp_like(" + genExpr(f.args.get(0)) + "," + genExpr(f.args.get(1)) + ")";
            }
            if (fname.equals("upper")) {
                String x = genExpr(f.args.get(0));
                return "(toStringSafe(" + x + ")==null?null:toStringSafe(" + x + ").toUpperCase())";
            }
            if (fname.equals("current_date")) return "current_date()";
            if (fname.equals("now")) return "now()";

            if (f.args == null || f.args.isEmpty()) return "null";
            return "toStringSafe(" + genExpr(f.args.get(0)) + ")";
        }

        if (e instanceof CastExpr c) return genExpr(c.expr);

        throw new IllegalStateException("Unhandled expr type: " + e.getClass());
    }

    private String escapeJava(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}