import java.util.*;

public class TaintAnalyzer {

    private final Map<String, String> taintedVariables = new HashMap<>();
    private static final Set<String> BLACKLIST = new HashSet<>(Arrays.asList(
            "java.lang.Runtime.exec",
            "java.lang.reflect.Method.invoke"
    ));
    private static final Set<String> TAINT_SOURCES = new HashSet<>(Arrays.asList(
            "getParameter", "getInputStream", "readLine"
    ));
    private static final Set<String> FILTERS = new HashSet<>(Arrays.asList(
            "sanitizeInput", "escapeHtml", "encodeForSQL"
    ));

    public void analyzeMethod(String classPath, String methodName, List<String> methodStatements) {
        System.out.println("Analyzing method: " + methodName + " in class: " + classPath);
        for (String stmt : methodStatements) {
            checkTaintSource(stmt, classPath, methodName);
            propagateTaint(stmt);
            checkFilterUsage(stmt);
            checkBlacklistedUsage(stmt, classPath, methodName);
        }
    }

    private void checkTaintSource(String stmt, String classPath, String methodName) {
        for (String source : TAINT_SOURCES) {
            if (stmt.contains(source)) {
                String variable = extractVariable(stmt);
                if (variable != null) {
                    taintedVariables.put(variable, "Source: " + source);
                    System.out.println("Tainted variable: " + variable + " (source: " + source + ") in method: " + methodName + " in class: " + classPath);
                }
            }
        }
    }

    private void propagateTaint(String stmt) {
        String[] parts = stmt.split("=");
        if (parts.length == 2) {
            String leftVar = parts[0].trim();
            String rightVar = parts[1].trim();
            if (taintedVariables.containsKey(rightVar)) {
                taintedVariables.put(leftVar, taintedVariables.get(rightVar));
                System.out.println("Variable " + leftVar + " is now tainted due to " + rightVar);
            }
        }
    }

    private void checkFilterUsage(String stmt) {
        for (String filter : FILTERS) {
            if (stmt.contains(filter)) {
                String variable = extractVariable(stmt);
                if (variable != null && taintedVariables.containsKey(variable)) {
                    taintedVariables.remove(variable);
                    System.out.println("Variable " + variable + " has been filtered by " + filter);
                }
            }
        }
    }

    private void checkBlacklistedUsage(String stmt, String classPath, String methodName) {
        for (String method : BLACKLIST) {
            if (stmt.contains(method)) {
                String[] parts = stmt.split("\\(");
                if (parts.length > 1) {
                    String params = parts[1].replace(")", "");
                    String[] variables = params.split(",");
                    for (String var : variables) {
                        if (taintedVariables.containsKey(var.trim())) {
                            System.out.println("Tainted data flow detected: " + var.trim() + 
                                               " used in blacklisted method " + method + 
                                               " in method: " + methodName + 
                                               " in class: " + classPath);
                        }
                    }
                }
            }
        }
    }

    private String extractVariable(String stmt) {
        String[] parts = stmt.split("=");
        if (parts.length > 1) {
            return parts[0].trim();
        }
        return null;
    }

    public static void main(String[] args) {
        String classPath = "com.example.malicious.WebShell";
        String methodName = "handleRequest"; // Example method name
        List<String> statements = Arrays.asList(
                "String input = getParameter(request, 'userInput');",
                "String safeInput = sanitizeInput(input);",
                "String cmd = safeInput;",
                "Runtime.getRuntime().exec(cmd);"
        );

        TaintAnalyzer analyzer = new TaintAnalyzer();
        analyzer.analyzeMethod(classPath, methodName, statements);
    }
}