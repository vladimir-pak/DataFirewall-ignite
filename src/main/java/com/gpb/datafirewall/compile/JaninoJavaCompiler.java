package com.gpb.datafirewall.compile;

import org.codehaus.janino.SimpleCompiler;

import java.util.Map;

/**
 * Компилятор в byte[] из Java Source (String)
 */
public final class JaninoJavaCompiler {
    /**
     * @param className полное имя класса, как в исходнике. Например: Rule123
     */
    public static byte[] compile(String className, String javaSource) {
        try {
            SimpleCompiler compiler = new SimpleCompiler();
            compiler.cook(javaSource); // компиляция

            // Карта "полное_имя_класса -> байткод"
            Map<String, byte[]> bytecodes = compiler.getBytecodes();

            byte[] bytes = bytecodes.get(className);
            if (bytes == null) {
                // полезно вывести, какие имена вообще сгенерированы
                throw new IllegalStateException(
                    "No bytecode found for class '" + className +
                    "'. Available: " + bytecodes.keySet()
                );
            }

            return bytes;
        } catch (Exception e) {
            throw new RuntimeException("Compilation failed for " + className, e);
        }
    }
}
