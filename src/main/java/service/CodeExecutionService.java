package service;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class CodeExecutionService {
    private static final String CLASS_NAME = "StudentCode";
    
    public String compileAndRun(String code) {
        try {
            // Create a JavaFileObject for the source code
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            
            String sourceCode = "public class " + CLASS_NAME + " {\n" +
                              "    public static void main(String[] args) {\n" +
                              code + "\n" +
                              "    }\n" +
                              "}";
            
            JavaFileObject source = new SimpleJavaFileObject(
                URI.create("string:///" + CLASS_NAME + ".java"),
                JavaFileObject.Kind.SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return sourceCode;
                }
            };
            
            // Compile the source code
            List<JavaFileObject> compilationUnits = new ArrayList<>();
            compilationUnits.add(source);
            
            StringWriter output = new StringWriter();
            JavaCompiler.CompilationTask task = compiler.getTask(
                output, null, diagnostics, null, null, compilationUnits);
            
            boolean success = task.call();
            
            if (!success) {
                // Compilation failed
                StringBuilder errorMessage = new StringBuilder();
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    errorMessage.append(diagnostic.getMessage(null)).append("\n");
                }
                return "Compilation Error:\n" + errorMessage.toString();
            }
            
            // Execute the compiled code
            try {
                // Redirect System.out to capture output
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                PrintStream oldOut = System.out;
                System.setOut(ps);
                
                // Load and run the compiled class
                Class<?> compiledClass = Class.forName(CLASS_NAME);
                compiledClass.getMethod("main", String[].class)
                    .invoke(null, (Object) new String[0]);
                
                // Restore System.out
                System.setOut(oldOut);
                
                return baos.toString();
            } catch (Exception e) {
                return "Runtime Error:\n" + e.getMessage();
            }
        } catch (Exception e) {
            return "Error:\n" + e.getMessage();
        }
    }
} 