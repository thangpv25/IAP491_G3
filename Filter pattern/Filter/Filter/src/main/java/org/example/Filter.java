package org.example;

import org.example.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {
    public static void main(String[] args) {
        String classPath = "src/test";
        System.out.println("Running Filter analysis on folder: " + classPath);
        try {
            FilterBlackList(classPath);
        } catch (IOException e) {
            LogUtils.logit("Error reading files: " + e.getMessage());
        }
    }

    // Define risky java reflection
    static List<String> riskReflection = List.of(
            ".getDeclaredMethod(\"defineClass",
            ".invoke("
    );

    // Define risky java sign new filter, listener, servlet, controller, interceptor
    static List<String> signFilter = List.of(
            "setFilterName(",
            "getDeclaredMethod(\"setFilterName"
    );

    static List<String> signListener = List.of(
            "addApplicationEventListener(",
            "getDeclaredMethod(\"addApplicationEventListener"
    );

    static List<String> signServlet = List.of(
            "addServletMapping(",
            "addServletMappingDecoded(",
            "ServletRegistration.Dynamic", //interface servlet tomcat => cho phép đăng ký mới một servlet
            "ApplicationServletRegistration"
    );

    static List<String> signController = List.of(
            "registerMapping(",
            "registerHandler(",
            "register\".equals",
            "getDeclaredMethod(\"registerHandler"
    );

    static List<String> signInterceptor = List.of(
            "getDeclaredField(\"adaptedInterceptors"
    );

    // Define risky keyword to execute command
    static List<String> riskKeyword = List.of(
            ".getRuntime().exec(",
            "/bin/bash",
            "/bin/sh",
            "cmd.exe",
            "powershell.exe"
    );

    // kiểm tra Filter
    private static boolean checkRiskFilter(String content, String fileName) {
        for (String newFilter : signFilter) {
            if (content.contains(newFilter)) {
                LogUtils.logit("Found risky Filter action in file: " + fileName + " - Keyword: " + newFilter + "Risk Point: 2");
                return true;  // Tìm thấy từ khóa nguy hiểm trong Filter
            }
        }
        return false;  // Không tìm thấy từ khóa nguy hiểm trong Filter
    }

    //kiểm tra Listener
    private static boolean checkRiskListener(String content, String fileName) {
        for (String newListener : signListener) {
            if (content.contains(newListener)) {
                LogUtils.logit("Found risky Listener action in file: " + fileName + " - Keyword: " + newListener + "Risk Point: 2");
                return true;  // Tìm thấy từ khóa nguy hiểm trong Listener
            }
        }
        return false;  // Không tìm thấy từ khóa nguy hiểm trong Listener
    }

    // kiểm tra trong Servlet
    private static boolean checkRiskServlet(String content, String fileName) {
        for (String newServlet : signServlet) {
            if (content.contains(newServlet)) {
                LogUtils.logit("Found risky Servlet action in file: " + fileName + " - Keyword: " + newServlet + "Risk Point: 2");
                return true;  // Tìm thấy từ khóa nguy hiểm trong Servlet
            }
        }
        return false;  // Không tìm thấy từ khóa nguy hiểm trong Servlet
    }

    // kiểm tra trong Spring Controller
    private static boolean checkRiskController(String content, String fileName) {
        for (String newSpringController : signController   ) {
            if (content.contains(newSpringController)) {
                LogUtils.logit("Found risky Spring Controller action in file: " + fileName + " - Keyword: " + newSpringController + "Risk Point: 2");
                return true;  // Tìm thấy từ khóa nguy hiểm trong Servlet
            }
        }
        return false;  // Không tìm thấy từ khóa nguy hiểm trong Spring
    }

    // kiểm tra trong Spring Interceptor
    private static boolean checkRiskInterceptor(String content, String fileName) {
        for (String newSpringInterceptor : signInterceptor   ) {
            if (content.contains(newSpringInterceptor)) {
                LogUtils.logit("Found risky Spring Controller action in file: " + fileName + " - Keyword: " + newSpringInterceptor + "Risk Point: 2");
                return true;  // Tìm thấy từ khóa nguy hiểm trong Spring
            }
        }
        return false;  // Không tìm thấy từ khóa nguy hiểm trong Spring
    }

    private static void FilterBlackList(String classPath) throws IOException {
        List<File> resultFiles = new ArrayList<>();
        List<Integer> Points = new ArrayList<>();

        // Load .java files from the specified directory
        List<File> loadedFiles = loadJavaFilesFromDirectory(classPath);

        // Define risky packages
        List<String> riskPackage = List.of("net.rebeyond.", "com.metasploit.");

        // Analyze each loaded .java file
        for (File file : loadedFiles) {
            // Tạo biểu thức chính quy để tìm tất cả các chuỗi trong dấu ngoặc kép
            String content = new String(Files.readAllBytes(file.toPath()));
            String regex = "\"([^\"]+)\"";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);


            String fileName = file.getName();
            boolean shouldAdd = false;
            int riskPoint = 0;

            // Check risk package
            if (content.contains("package ")) {
                String packageName = content.split("\n")[0].replace("package ", "").replace(";", "").trim();
                for (String packagePrefix : riskPackage) {
                    if (packageName.startsWith(packagePrefix)) {
                        shouldAdd = true;
                        riskPoint += 5;
                        LogUtils.logit("Found risky package in: " + fileName + " - Risk Point: " + riskPoint);
                        break;
                    }
                }
            }

            //check risky keyword
            if (!shouldAdd) {  // Chỉ kiểm tra keyword nếu chưa đánh dấu là risky
                for (String keyword : riskKeyword) {
                    if (content.contains(keyword)) {
                        shouldAdd = true;
                        riskPoint += 5;
                        LogUtils.logit("Found risky behaviour in: " + fileName + " - Risk Point: " + riskPoint);
                        break;
                    }
                }
            }

            // Kiểm tra và in ra các chuỗi có độ dài lớn hơn 100 ký tự
            if (!shouldAdd) {
                List<String> longStrings = new ArrayList<>();
                while (matcher.find()) {
                    String str = matcher.group(1); // Lấy chuỗi trong dấu ngoặc kép
                    if (str.length() > 100) {
                        longStrings.add(str);
                    }
                }
                if (!longStrings.isEmpty()) {
                    riskPoint += 1;
                    LogUtils.logit("Found large String in: " + fileName + " - Risk Point: " + riskPoint);
                }
            }

            //Kiểm tra sử dụng base64lib
            if (!shouldAdd) {
                if(content.contains("sun.misc.BASE64Decoder") || content.contains("java.util.Base64")) {
                    riskPoint += 1;
                    LogUtils.logit("Found the use of base64 lib in the file: " + fileName + " - Risk Point: " + riskPoint);
                }
            }

//          Check dynamic invoke class (java reflection)
            if (!shouldAdd) {
                    boolean allKeywordsFound = true;
                    for (String reflection : riskReflection) {
                        if (!content.contains(reflection)) {
                            allKeywordsFound = false;
                            break;
                        }
                    }
                    if (allKeywordsFound) {
                        riskPoint += 2;
                        boolean filterFound = false;
                        boolean listenerFound = false;
                        boolean servletFound = false;
                        boolean controllerFound = false;
                        boolean interceptorFound = false;

                        //Kiểm tra Filter
                        if (checkRiskFilter(content, fileName)) {
                            riskPoint += 1;
                            shouldAdd = true;
                            filterFound = true;
                            LogUtils.logit("Found risky sign new Filter action in file: " + fileName + " - riskPoint: " + riskPoint);
                        }

                        // Kiểm tra Listener
                        if (checkRiskListener(content, fileName)) {
                            riskPoint += 1;
                            shouldAdd = true;
                            listenerFound = true;
                            LogUtils.logit("Found risky sign new Listener action in file: " + fileName + " - riskPoint: " + riskPoint);
                        }

                        // Kiểm tra  Servlet
                        if (checkRiskServlet(content, fileName)) {
                            riskPoint += 1;
                            shouldAdd = true;
                            servletFound = true;
                            LogUtils.logit("Found risky sign new Servlet action in file: " + fileName + " - riskPoint: " + riskPoint);
                        }

                        //Kiểm tra Spring Boot Controller
                        if (checkRiskController(content, fileName)) {
                            riskPoint += 1;
                            shouldAdd = true;
                            controllerFound = true;
                            LogUtils.logit("Found risky sign new Controller action in file: " + fileName + " - riskPoint: " + riskPoint);
                        }

                        //Kiểm tra Spring Boot Interceptor
                        if (checkRiskInterceptor(content, fileName)) {
                            riskPoint += 1;
                            shouldAdd = true;
                            interceptorFound = true;
                            LogUtils.logit("Found risky sign new Interceptor action in file: " + fileName + " - riskPoint: " + riskPoint);
                        }

                        //Đánh dấu file chỉ dynamical loading class
                        if (!filterFound && !listenerFound && !servletFound && !interceptorFound && !controllerFound) {
                            LogUtils.logit("File dynamical loading class via java reflection: " + fileName + " - Risk Point: " + riskPoint);
                            shouldAdd = true;
                        }
                    }

            }


            // If any risky condition is found, add to results
            if (shouldAdd) {
                resultFiles.add(file);
                Points.add(riskPoint);

            }
        }

        // Display results
        String results = "All Suspicious Files   : " + resultFiles.size() + "\n\n";
        StringBuilder high_level = new StringBuilder("============================================================\n" +
                "High risk level Files   : \n");

        int order = 1;
        for (int i = 0; i < resultFiles.size(); i++) {
            File file = resultFiles.get(i);
            Integer point = Points.get(i); // Lấy điểm từ array Points tương ứng

            // Đọc nội dung file (nếu cần thiết)
            String content = new String(Files.readAllBytes(file.toPath()));

            // Format kết quả
            String tmp = String.format(
                    "Order       : %d\n" +
                            "Name        : %s\n" +
                            "Risk level  : %d\n" +  // In giá trị của Points (risk level)
                            "Location    : %s\n\n",
                    order,
                    file.getName(),
                    point,  // Hiển thị điểm nguy cơ từ Points
                    file.getAbsolutePath()
            );

            // Thêm kết quả vào danh sách high_level
            high_level.append(tmp);

            order += 1;
        }

        // Log the results
        LogUtils.result(results + high_level);
        LogUtils.logit("Analysis complete. Results written to log file.");
    }


    private static List<File> loadJavaFilesFromDirectory(String classPath) throws IOException {
        List<File> loadedFiles = new ArrayList<>();
        Path directory = Paths.get(classPath);

        // Load .java files from the specified directory and its subdirectories
        Files.walk(directory)
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".java") || file.toString().endsWith(".jsp"))
                .forEach(file -> loadedFiles.add(file.toFile()));

        return loadedFiles;
    }
}
