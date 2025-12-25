package com.gpb.datafirewall.parser;

import com.gpb.datafirewall.parser.ast.AndExpr;
import com.gpb.datafirewall.parser.ast.CastExpr;
import com.gpb.datafirewall.parser.ast.CompareExpr;
import com.gpb.datafirewall.parser.ast.Expr;
import com.gpb.datafirewall.parser.ast.FieldExpr;
import com.gpb.datafirewall.parser.ast.FuncExpr;
import com.gpb.datafirewall.parser.ast.InExpr;
import com.gpb.datafirewall.parser.ast.IsNotNullExpr;
import com.gpb.datafirewall.parser.ast.IsNullExpr;
import com.gpb.datafirewall.parser.ast.LikeExpr;
import com.gpb.datafirewall.parser.ast.NotExpr;
import com.gpb.datafirewall.parser.ast.NumberExpr;
import com.gpb.datafirewall.parser.ast.OrExpr;
import com.gpb.datafirewall.parser.ast.StringExpr;

/**
 * Генератор Java Source (код Java в String) на основе AST
 */
public class JavaRuleGenerator {

    public String generate(String className, Expr expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("import java.util.*;\nimport java.util.regex.*;\nimport com.gpb.datafirewall.model.Rule;\n");
        sb.append("public class ").append(className).append(" implements Rule {\n");
        // helper methods
        sb.append("  private String toStringSafe(Object o){return o==null?null:o.toString();}\n");
        sb.append("  private Double toDoubleOrNull(Object v){ try{ return v==null?null:Double.valueOf(v.toString());}catch(Exception e){return null;} }\n");
        sb.append("  private int utf8Length(String s){ return s==null?0:s.getBytes(java.nio.charset.StandardCharsets.UTF_8).length; }\n");
        sb.append("  private boolean regexp_like(String s, String pat){ if(s==null) return false; try{ return Pattern.compile(pat).matcher(s).find(); }catch(Exception e){return false;} }\n");

        sb.append("  public boolean apply(java.util.Map<String,String> data){\n");
        sb.append("    try{\n");
        sb.append("      return ").append(genExpr(expr)).append(";\n");
        sb.append("    } catch(Exception e){ return false; }\n");
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String genExpr(Expr e) {
        if (e instanceof FieldExpr) {
            String key = ((FieldExpr)e).name;
            return "toStringSafe(data.get(\"" + escapeJava(key) + "\"))";
        }
        if (e instanceof StringExpr) {
            return "\"" + escapeJava(((StringExpr)e).value) + "\"";
        }
        if (e instanceof NumberExpr) {
            return ((NumberExpr)e).value;
        }
        if (e instanceof CompareExpr) {
            CompareExpr c = (CompareExpr)e;
            String l = genExpr(c.left);
            String r = genExpr(c.right);
            switch(c.op) {
                case "=": return "(" + l + "!=null && " + l + ".equals(" + r + "))";
                case "!=": case "<>": return "(" + l + "==null || !" + l + ".equals(" + r + "))";
                case ">": return "( (toDoubleOrNull(" + l + ")!=null) && (toDoubleOrNull(" + r + ")!=null) && toDoubleOrNull(" + l + ") > toDoubleOrNull(" + r + ") )";
                case "<": return "( (toDoubleOrNull(" + l + ")!=null) && (toDoubleOrNull(" + r + ")!=null) && toDoubleOrNull(" + l + ") < toDoubleOrNull(" + r + ") )";
                case ">=": return "( (toDoubleOrNull(" + l + ")!=null) && (toDoubleOrNull(" + r + ")!=null) && toDoubleOrNull(" + l + ") >= toDoubleOrNull(" + r + ") )";
                case "<=": return "( (toDoubleOrNull(" + l + ")!=null) && (toDoubleOrNull(" + r + ")!=null) && toDoubleOrNull(" + l + ") <= toDoubleOrNull(" + r + ") )";
            }
        }
        if (e instanceof IsNullExpr) {
            return "(" + genExpr(((IsNullExpr)e).expr) + "==null)";
        }
        if (e instanceof IsNotNullExpr) {
            return "(" + genExpr(((IsNotNullExpr)e).expr) + "!=null)";
        }
        if (e instanceof LikeExpr) {
            LikeExpr l = (LikeExpr) e;
            String val = genExpr(l.value);
            String regex = escapeJava(l.pattern)
                    .replace("%", ".*")
                    .replace("_", ".");
            return "(toStringSafe(" + val + ")!=null && toStringSafe(" + val + ").matches(\"" + regex + "\"))";
        }
        if (e instanceof InExpr) {
            InExpr in = (InExpr) e;
            String val = "toStringSafe(" + genExpr(in.value) + ")";
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < in.options.size(); i++) {
                String opt = genExpr(in.options.get(i));
                if (i>0) sb.append(" || ");
                sb.append(val).append("!=null && ").append(val).append(".equals(").append(opt).append(")");
            }
            if (in.options.isEmpty()) {
                sb.append("false");
            }
            sb.append(")");
            return sb.toString();
        }
        if (e instanceof AndExpr) {
            return "(" + genExpr(((AndExpr)e).left) + " && " + genExpr(((AndExpr)e).right) + ")";
        }
        if (e instanceof OrExpr) {
            return "(" + genExpr(((OrExpr)e).left) + " || " + genExpr(((OrExpr)e).right) + ")";
        }
        if (e instanceof NotExpr) {
            return "(!" + genExpr(((NotExpr)e).expr) + ")";
        }
        if (e instanceof FuncExpr) {
            FuncExpr f = (FuncExpr)e;
            String fname = f.funcName.toLowerCase();
            // handle common functions
            if (fname.equals("syslib.utf8_length") || fname.equals("utf8_length")) {
                return "utf8Length(" + genExpr(f.args.get(0)) + ")";
            }
            if (fname.equals("regexp_like")) {
                return "regexp_like(" + genExpr(f.args.get(0)) + "," + genExpr(f.args.get(1)) + ")";
            }
            if (fname.equals("upper")) {
                return "toStringSafe(" + genExpr(f.args.get(0)) + ").toUpperCase()";
            }
            if (fname.equals("now") || fname.equals("current_timestamp")) {
                return "java.lang.System.currentTimeMillis()";
            }
            if (fname.equals("cast")) {
                // but cast handled earlier as CastExpr; fallback to toString
                return "toStringSafe(" + genExpr(f.args.get(0)) + ")";
            }
            // generic: call toStringSafe
            if (f.args.isEmpty()) {
                return "null";
            }
            return "toStringSafe(" + genExpr(f.args.get(0)) + ")";
        }
        if (e instanceof CastExpr) {
            CastExpr c = (CastExpr) e;
            String inner = genExpr(c.expr);
            // only support CAST(... AS string) for now
            return "toStringSafe(" + inner + ")";
        }
        // fallback
        throw new IllegalStateException("Unhandled expr type: " + e.getClass());
    }

    private String escapeJava(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
    }
}
