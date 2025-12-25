package com.gpb.datafirewall.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DiagnosticErrorListener;

import com.gpb.datafirewall.parser.JavaRuleGenerator;
import com.gpb.datafirewall.parser.ast.AstBuilder;
import com.gpb.datafirewall.parser.ast.Expr;
// import com.gpb.datafirewall.parser.ir.AstToIr;
// import com.gpb.datafirewall.parser.ir.IRExpr;
import com.gpb.datafirewall.rules.parser.SqlWhereLexer;
import com.gpb.datafirewall.rules.parser.SqlWhereParser;

import lombok.extern.slf4j.Slf4j;

/**
 * Пайплайн для генерации byte[] из AST
 */
@Slf4j
public class RuleCompilerPipeline {

    private final ExecutorService pool;
    private final JavaRuleGenerator generator;

    public RuleCompilerPipeline(int threads) {
        this.pool = Executors.newFixedThreadPool(threads);
        this.generator = new JavaRuleGenerator();
    }

    public Map<String, byte[]> process(Map<Integer, String> rulesSql) {

        Map<Integer, Future<Map<String, byte[]>>> futures = new HashMap<>();

        for (var entry : rulesSql.entrySet()) {
            int ruleId = entry.getKey();
            String sql = entry.getValue();

            futures.put(ruleId, pool.submit(() -> processOne(ruleId, sql)));
        }

        // собираем результат в единый Map: className → byte[]
        Map<String, byte[]> result = new HashMap<>();

        for (var e : futures.entrySet()) {
            try {
                Map<String, byte[]> compiled = e.getValue().get();
                if (compiled == null) {
                    throw new RuntimeException("Rule " + e.getKey() + " produced null compiled map");
                }
                // merged compiled classes
                result.putAll(compiled);
            } catch (Exception ex) {
                throw new RuntimeException("Rule " + e.getKey() + " failed", ex);
            }
        }

        return result;
    }

    /** Обработка одного правила: SQL → Expr → Java → byte[] */
    private Map<String, byte[]> processOne(int id, String sql) {
        try {
            log.info("Processing rule {} with SQL: {}", id, sql);
            // 1. Парсим SQL
            SqlWhereLexer lexer = new SqlWhereLexer(CharStreams.fromString(sql));
            SqlWhereParser parser = new SqlWhereParser(new CommonTokenStream(lexer));

            parser.removeErrorListeners();
            parser.addErrorListener(new DiagnosticErrorListener());

            SqlWhereParser.ParseContext tree = parser.parse();
            log.info("Syntax errors: {}", parser.getNumberOfSyntaxErrors());

            // строим AST по уже распарсенному дереву, не вызывая parse() повторно
            Expr ast = new AstBuilder().visit(tree);

            // 2. Генерируем Java-класс через JavaCompiler - долго
            String className = "Rule" + id;
            String javaSrc = generator.generate(className, ast);
            byte[] bytecode = JaninoJavaCompiler.compile(className, javaSrc);
            Map<String, byte[]> map = new HashMap<>();
            map.put(className, bytecode);
            // создаём FileManager на каждый компиляторный вызов!
            return map;

        } catch (Exception ex) {
            log.error("Error processing rule {} with SQL: {}", id, sql, ex);
            throw new RuntimeException("Error processing rule " + id, ex);
        }
    }

    public void shutdown() {
        pool.shutdown();
    }
}
