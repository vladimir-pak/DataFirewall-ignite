package com.gpb.datafirewall.compile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DiagnosticErrorListener;

import com.gpb.datafirewall.parser.JavaRuleGenerator;
import com.gpb.datafirewall.parser.ast.AstBuilder;
import com.gpb.datafirewall.parser.ast.Expr;
import com.gpb.datafirewall.rules.parser.SqlWhereLexer;
import com.gpb.datafirewall.rules.parser.SqlWhereParser;

import lombok.extern.slf4j.Slf4j;

/**
 * Пайплайн для генерации byte[] из SQL:
 * SQL -> ANTLR parse -> AST -> Java source -> Janino compile -> byte[]
 *
 * Дополнительно:
 * - сохраняет generated Java source в .java файлы
 *
 * Важно:
 * - НЕ валит весь пайплайн, если одно правило не собирается (continue-on-error).
 * - Если у правила есть syntax errors на уровне ANTLR, правило пропускается.
 */
@Slf4j
public class RuleCompilerPipeline {

    private final ExecutorService pool;
    private final JavaRuleGenerator generator;
    private final Path outputDir;

    public RuleCompilerPipeline(int threads) {
        this(threads, "generated-rules");
    }

    public RuleCompilerPipeline(int threads, String outputDir) {
        int safeThreads = Math.max(1, threads);
        this.pool = Executors.newFixedThreadPool(safeThreads);
        this.generator = new JavaRuleGenerator();
        this.outputDir = Path.of(outputDir);

        try {
            Files.createDirectories(this.outputDir);
            log.info("Generated rules directory: {}", this.outputDir.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot create output dir for generated rules: " + this.outputDir.toAbsolutePath(), e
            );
        }
    }

    /**
     * Компилирует набор правил (id -> sql) в Map (className -> bytecode).
     * Ошибочные правила пропускаются, остальные возвращаются.
     */
    public Map<String, byte[]> process(Map<Integer, String> rulesSql) {
        if (rulesSql == null || rulesSql.isEmpty()) {
            return Map.of();
        }

        Map<Integer, Future<Map<String, byte[]>>> futures = new HashMap<>();

        for (var entry : rulesSql.entrySet()) {
            final int ruleId = entry.getKey();
            final String sql = entry.getValue();

            futures.put(ruleId, pool.submit(() -> processOne(ruleId, sql)));
        }

        Map<String, byte[]> result = new HashMap<>();
        int ok = 0;
        int failed = 0;
        int skipped = 0;

        for (var e : futures.entrySet()) {
            int ruleId = e.getKey();
            try {
                Map<String, byte[]> compiled = e.getValue().get();

                if (compiled == null || compiled.isEmpty()) {
                    skipped++;
                    continue;
                }

                result.putAll(compiled);
                ok++;
            } catch (Exception ex) {
                failed++;
                log.error("Rule {} failed (future/get). Skipping.", ruleId, ex);
            }
        }

        log.info("RuleCompilerPipeline done: inputRules={}, compiledOk={}, failed={}, skipped={}, totalClasses={}",
                rulesSql.size(), ok, failed, skipped, result.size());

        return result;
    }

    /**
     * Обработка одного правила: SQL -> Expr -> Java -> byte[]
     *
     * Возвращает:
     * - Map.of() если правило нужно пропустить (syntax errors / пустое sql / и т.п.)
     * - Map(className -> bytecode) если успешно
     *
     * НЕ бросает исключения наружу (чтобы не ломать batch).
     */
    private Map<String, byte[]> processOne(int id, String sql) {
        String className = "Rule" + id;

        try {
            if (sql == null || sql.isBlank()) {
                log.warn("Rule {} has empty SQL. Skipping.", id);
                return Map.of();
            }

            log.info("Processing rule {} with SQL: {}", id, sql);

            // 1) Парсим SQL (ANTLR)
            SqlWhereLexer lexer = new SqlWhereLexer(CharStreams.fromString(sql));
            SqlWhereParser parser = new SqlWhereParser(new CommonTokenStream(lexer));

            parser.removeErrorListeners();
            parser.addErrorListener(new DiagnosticErrorListener());

            SqlWhereParser.ParseContext tree = parser.parse();
            int syntaxErrors = parser.getNumberOfSyntaxErrors();
            log.info("Rule {} syntax errors: {}", id, syntaxErrors);

            if (syntaxErrors > 0) {
                log.warn("Rule {} has {} syntax errors. Skipping compilation.", id, syntaxErrors);
                return Map.of();
            }

            // 2) AST
            Expr ast = new AstBuilder().visit(tree);

            // 3) Генерация Java source
            String javaSrc = generator.generate(className, ast);

            // 4) Сохраняем .java
            saveJavaSource(className, javaSrc);

            // 5) Компилируем в bytecode
            byte[] bytecode = JaninoJavaCompiler.compile(className, javaSrc);

            Map<String, byte[]> out = new HashMap<>(1);
            out.put(className, bytecode);
            return out;

        } catch (Exception ex) {
            log.error("Error processing rule {} with SQL: {}. Skipping rule.", id, sql, ex);
            return Map.of();
        }
    }

    private void saveJavaSource(String className, String javaSrc) {
        try {
            Path file = outputDir.resolve(className + ".java");
            Files.writeString(file, javaSrc, StandardCharsets.UTF_8);
            log.info("Saved generated rule source: {}", file.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save generated Java source for {}", className, e);
        }
    }

    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pool.shutdownNow();
        }
    }
}