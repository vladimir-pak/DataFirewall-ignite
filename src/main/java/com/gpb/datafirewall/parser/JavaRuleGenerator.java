package com.gpb.datafirewall.parser;

import com.gpb.datafirewall.parser.ast.*;
import java.util.*;
import java.util.regex.*;

/**
 * Генератор Java Source (код Java в String) на основе AST
 */
public class JavaRuleGenerator {

    public String generate(String className, Expr expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("import java.util.*;\n");
        sb.append("import java.util.regex.*;\n");
        sb.append("import com.gpb.datafirewall.model.Rule;\n");

        sb.append("public class ").append(className).append(" implements Rule {\n");

        // helpers
        sb.append("  private String toStringSafe(Object o){ return o==null?null:o.toString(); }\n");
        sb.append("  private Double toDoubleOrNull(Object v){\n");
        sb.append("    try { return v==null?null:Double.valueOf(v.toString()); }\n");
        sb.append("    catch(Exception e){ return null; }\n");
        sb.append("  }\n");

        // IMPORTANT: Integer, not int (nullable!)
        sb.append("  private Integer utf8Length(String s){\n");
        sb.append("    return s==null ? null : Integer.valueOf(s.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);\n");
        sb.append("  }\n");

        sb.append("  private boolean regexp_like(String s, String pat){\n");
        sb.append("    if(s==null) return false;\n");
        sb.append("    try{ return Pattern.compile(pat).matcher(s).find(); }catch(Exception e){ return false; }\n");
        sb.append("  }\n");

        // numeric compare helpers
        sb.append("  private boolean numEq(Object a, Object b){\n");
        sb.append("    Double da = toDoubleOrNull(a); Double db = toDoubleOrNull(b);\n");
        sb.append("    return da != null && db != null && Double.compare(da, db) == 0;\n");
        sb.append("  }\n");
        sb.append("  private boolean numNe(Object a, Object b){\n");
        sb.append("    Double da = toDoubleOrNull(a); Double db = toDoubleOrNull(b);\n");
        sb.append("    return da != null && db != null && Double.compare(da, db) != 0;\n");
        sb.append("  }\n");
        sb.append("  private boolean numGt(Object a, Object b){\n");
        sb.append("    Double da = toDoubleOrNull(a); Double db = toDoubleOrNull(b);\n");
        sb.append("    return da != null && db != null && da > db;\n");
        sb.append("  }\n");
        sb.append("  private boolean numLt(Object a, Object b){\n");
        sb.append("    Double da = toDoubleOrNull(a); Double db = toDoubleOrNull(b);\n");
        sb.append("    return da != null && db != null && da < db;\n");
        sb.append("  }\n");
        sb.append("  private boolean numGe(Object a, Object b){\n");
        sb.append("    Double da = toDoubleOrNull(a); Double db = toDoubleOrNull(b);\n");
        sb.append("    return da != null && db != null && da >= db;\n");
        sb.append("  }\n");
        sb.append("  private boolean numLe(Object a, Object b){\n");
        sb.append("    Double da = toDoubleOrNull(a); Double db = toDoubleOrNull(b);\n");
        sb.append("    return da != null && db != null && da <= db;\n");
        sb.append("  }\n");

        sb.append("  public boolean apply(java.util.Map<String,String> data){\n");
        sb.append("    try{\n");
        sb.append("      return ").append(genExpr(expr)).append(";\n");
        sb.append("    } catch(Exception e){ return false; }\n");
        sb.append("  }\n");
        sb.append("}\n");

        return sb.toString();
    }

    private boolean isNumericExpr(Expr e) {
        if (e instanceof NumberExpr) return true;
        if (e instanceof FuncExpr f) {
            String fn = f.funcName.toLowerCase();
            return fn.equals("syslib.utf8_length") || fn.equals("utf8_length") || fn.equals("length");
        }
        return false;
    }

    private String genExpr(Expr e) {
        if (e instanceof FieldExpr fe) {
            return "toStringSafe(data.get(\"" + escapeJava(fe.name) + "\"))";
        }
        if (e instanceof StringExpr se) {
            return "\"" + escapeJava(se.value) + "\"";
        }
        if (e instanceof NumberExpr ne) {
            return ne.value; // как текст числа
        }
        if (e instanceof CompareExpr ce) {
            String l = genExpr(ce.left);
            String r = genExpr(ce.right);

            boolean numeric = isNumericExpr(ce.left) || isNumericExpr(ce.right)
                    || ce.op.equals(">") || ce.op.equals("<") || ce.op.equals(">=") || ce.op.equals("<=");

            if (numeric) {
                return switch (ce.op) {
                    case "="  -> "numEq(" + l + "," + r + ")";
                    case "!=" , "<>" -> "numNe(" + l + "," + r + ")";
                    case ">"  -> "numGt(" + l + "," + r + ")";
                    case "<"  -> "numLt(" + l + "," + r + ")";
                    case ">=" -> "numGe(" + l + "," + r + ")";
                    case "<=" -> "numLe(" + l + "," + r + ")";
                    default -> throw new IllegalStateException("Unknown op: " + ce.op);
                };
            }

            // string compare (nullable safe)
            return switch (ce.op) {
                case "=" -> "(" + l + "!=null && " + l + ".equals(" + r + "))";
                case "!=" , "<>" -> "(" + l + "==null || !" + l + ".equals(" + r + "))";
                default -> throw new IllegalStateException("Unsupported op for strings: " + ce.op);
            };
        }

        if (e instanceof IsNullExpr ine) {
            return "(" + genExpr(ine.expr) + "==null)";
        }
        if (e instanceof IsNotNullExpr inne) {
            return "(" + genExpr(inne.expr) + "!=null)";
        }
        if (e instanceof LikeExpr le) {
            String val = genExpr(le.value);
            String regex = escapeJava(le.pattern).replace("%", ".*").replace("_", ".");
            return "(toStringSafe(" + val + ")!=null && toStringSafe(" + val + ").matches(\"" + regex + "\"))";
        }
        if (e instanceof InExpr in) {
            String val = "toStringSafe(" + genExpr(in.value) + ")";
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < in.options.size(); i++) {
                if (i > 0) sb.append(" || ");
                sb.append(val).append("!=null && ").append(val).append(".equals(").append(genExpr(in.options.get(i))).append(")");
            }
            if (in.options.isEmpty()) sb.append("false");
            sb.append(")");
            return sb.toString();
        }
        if (e instanceof AndExpr ae) {
            return "(" + genExpr(ae.left) + " && " + genExpr(ae.right) + ")";
        }
        if (e instanceof OrExpr oe) {
            return "(" + genExpr(oe.left) + " || " + genExpr(oe.right) + ")";
        }
        if (e instanceof NotExpr ne) {
            return "(!" + genExpr(ne.expr) + ")";
        }
        if (e instanceof FuncExpr f) {
            String fname = f.funcName.toLowerCase();

            if (fname.equals("syslib.utf8_length") || fname.equals("utf8_length") || fname.equals("length")) {
                return "utf8Length(" + genExpr(f.args.get(0)) + ")";
            }
            if (fname.equals("regexp_like")) {
                return "regexp_like(" + genExpr(f.args.get(0)) + "," + genExpr(f.args.get(1)) + ")";
            }
            if (fname.equals("upper")) {
                return "(toStringSafe(" + genExpr(f.args.get(0)) + ")==null?null:toStringSafe(" + genExpr(f.args.get(0)) + ").toUpperCase())";
            }
            if (fname.equals("cast")) {
                return "toStringSafe(" + genExpr(f.args.get(0)) + ")";
            }

            if (f.args.isEmpty()) return "null";
            return "toStringSafe(" + genExpr(f.args.get(0)) + ")";
        }

        if (e instanceof CastExpr c) {
            return "toStringSafe(" + genExpr(c.expr) + ")";
        }

        throw new IllegalStateException("Unhandled expr type: " + e.getClass());
    }

    private String escapeJava(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
    }
}
