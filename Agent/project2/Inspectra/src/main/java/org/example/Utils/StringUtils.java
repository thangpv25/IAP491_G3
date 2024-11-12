package org.example.Utils;


import java.util.Map;

import static org.example.Loader.Contraints.AGENT_LOADER_FILE_NAME;
import static org.example.Loader.Contraints.AGENT_NAME;

public class StringUtils {
    public static String toLowerCase(String str) {
        return str.toLowerCase();
    }
    public static void println(String str) {
        System.out.println("[ " + AGENT_NAME + " ] " + str);
    }
    public static void printUsage() {
        System.out.println(AGENT_NAME + " (Java Agent)");
        System.out.println("Usage: java -jar " + AGENT_LOADER_FILE_NAME + " [Options]");
        System.out.println("  1) detach [Java PID]");
        System.out.println("  2) attach [Java PID]");
        System.out.println("\r\n");
        System.out.println("EXAMPLES :");
        System.out.println("  java -jar " + AGENT_LOADER_FILE_NAME + " attach 10001");
        System.out.println("  java -jar " + AGENT_LOADER_FILE_NAME + " detach 10001");
        System.out.println("\r\n");
        System.out.println("JVM PID List:");
    }
    public static String[] convertToStringArray(String str) {
        return str.replaceAll("[\\[\\]]", "").split(",\\s*");
    }

    public static void printProcessList(Map<String, String> processMap) {
        for (Map.Entry<String, String> entry : processMap.entrySet()) {
            System.out.println("PID: " + entry.getKey() + " - Process Name: " + entry.getValue());
        }
    }
    private static void printLogo() {
        String banner = getBanner();
        System.out.println("\n" + banner + "\n[ " + AGENT_NAME + " v1.0.0 ] by ka1t0_k1d\n");
    }

    /**
     * 获取Agent Banner信息
     *
     * @return 获取Banner
     */
    public static String getBanner() {
        return "\n" +
                "  _____        _____                 _             \n" +
                " |_   _|      / ____|               | |            \n" +
                "   | |  _ __ | (___  _ __   ___  ___| |_ _ __ __ _ \n" +
                "   | | | '_ \\ \\___ \\| '_ \\ / _ \\/ __| __| '__/ _` |\n" +
                "  _| |_| | | |____) | |_) |  __/ (__| |_| | | (_| |\n" +
                " |_____|_| |_|_____/| .__/ \\___|\\___|\\__|_|  \\__,_|\n" +
                "                    | |                            \n" +
                "                    |_|                            \n";
    }
}
