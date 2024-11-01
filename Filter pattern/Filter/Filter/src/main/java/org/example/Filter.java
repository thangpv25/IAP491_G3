package org.example;

import org.example.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

    private static void FilterBlackList(String classPath) throws IOException {
        List<File> resultFiles = new ArrayList<>();

        // Load .java files from the specified directory
        List<File> loadedFiles = loadJavaFilesFromDirectory(classPath);

        // Define risky superclasses, packages, annotations, and keywords
        List<String> riskPackage = List.of("net.rebeyond.", "com.metasploit.");
        List<String> riskAnnotations = List.of(
                "org.springframework.stereotype.Controller",
                "org.springframework.web.bind.annotation.RestController",
                "org.springframework.web.bind.annotation.RequestMapping",
                "org.springframework.web.bind.annotation.GetMapping",
                "org.springframework.web.bind.annotation.PostMapping",
                "org.springframework.web.bind.annotation.PatchMapping",
                "org.springframework.web.bind.annotation.PutMapping",
                "org.springframework.web.bind.annotation.Mapping"
        );
        List<String> riskKeyword = List.of(
                "com.metasploit.",
                "net.rebeyond.",
                "javax.crypto.",
                "ProcessBuilder",
                "getRuntime",
                "shell",
                "exec("
        );

        // Analyze each loaded .java file
        for (File file : loadedFiles) {
            String content = new String(Files.readAllBytes(file.toPath()));
            String fileName = file.getName();
            boolean shouldAdd = false;

            // Check package name (assumes package name is the first line in the file)
            if (content.startsWith("package ")) {
                String packageName = content.split("\n")[0].replace("package ", "").replace(";", "").trim();
                for (String packagePrefix : riskPackage) {
                    if (packageName.startsWith(packagePrefix)) {
                        shouldAdd = true;
                        LogUtils.logit("Found risky package in: " + fileName);
                        break;
                    }
                }
            }

            // Check for annotations
            for (String annotation : riskAnnotations) {
                if (content.contains("@" + annotation.substring(annotation.lastIndexOf('.') + 1))) {
                    shouldAdd = true;
                    LogUtils.logit("Found risky annotation in: " + fileName);
                    break;
                }
            }

            // Check for keywords
            for (String keyword : riskKeyword) {
                if (content.contains(keyword)) {
                    shouldAdd = true;
                    LogUtils.logit("Found risky keyword in: " + fileName);
                    break;
                }
            }

            // If any risky condition is found, add to results
            if (shouldAdd) {
                resultFiles.add(file);
            }
        }

        // Display results
        String results = "All Suspicious Files   : " + resultFiles.size() + "\n\n";
        String high_level = "============================================================\n" +
                "High risk level Files   : \n";
        String normal_level = "============================================================\n" +
                "Normal risk level Files : \n";

        int order = 1;
        for (File file : resultFiles) {
            String level = "normal"; // Default level
            String content = new String(Files.readAllBytes(file.toPath()));

            // Check for keywords in the content
            for (String keyword : riskKeyword) {
                if (content.contains(keyword)) {
                    level = "high";
                    break;
                }
            }

            // Format result
            String tmp = String.format(
                    "Order       : %d\n" +
                            "Name        : %s\n" +
                            "Risk level  : %s\n" +
                            "Location    : %s\n\n",
                    order ,
                    file.getName(),
                    level,
                    file.getAbsolutePath()
            );

            // Categorize by risk level
            if (level.equals("high")) {
                high_level += tmp;
            } else {
                normal_level += tmp;
            }
            order += 1;
        }

        // Log the results
        LogUtils.result(results + high_level + normal_level);
        LogUtils.logit("Analysis complete. Results written to log file.");
    }

    private static List<File> loadJavaFilesFromDirectory(String classPath) throws IOException {
        List<File> loadedFiles = new ArrayList<>();
        Path directory = Paths.get(classPath);

        // Load .java files from the specified directory and its subdirectories
        Files.walk(directory)
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".java"))
                .forEach(file -> loadedFiles.add(file.toFile()));

        return loadedFiles;
    }
}