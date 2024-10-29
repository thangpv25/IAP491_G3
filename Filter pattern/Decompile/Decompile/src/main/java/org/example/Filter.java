package org.example;

import org.example.common.Decompiler;
import org.example.utils.ClassUtils;
import org.example.utils.LogUtils;
import org.example.utils.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Filter {
    // public static File agent_work_directory = null;
    public static void main(String[] args) {
        List<Class<?>> loadedClasses = new ArrayList<>();
        loadedClasses.add(TestServlet.class);
        System.out.println("Running Filter analysis...");
        FilterBlackList(loadedClasses);
    }


    private static void Decompile(List<Class<?>> resultClasses, Class<?> clazz) {
        File dumpJavaFile = PathUtils.getStorePath(clazz, false);
        String source = Decompiler.decompile(clazz.getName(), null, false);
        PathUtils.writeByteArrayToFile(dumpJavaFile, source.getBytes());
        LogUtils.logit("Store java: " + clazz.getName() + " to " + dumpJavaFile.getAbsolutePath());
        resultClasses.add(clazz);
    }

    private static void FilterBlackList(List<Class<?>> loadedClasses) {
        List<Class<?>> resultClasses = new ArrayList<>();

        // Nhận tất cả các lớp đã tải và tên lớp
        List<String> loadedClassesNames = new ArrayList<>();
        for (Class<?> cls : loadedClasses) {
            loadedClassesNames.add(cls.getName());
        }

        // Tên lớp cha có thể thực hiện chức năng web shell
        List<String> riskSuperClassesName = new ArrayList<>();
        riskSuperClassesName.add("javax.servlet.http.HttpServlet");

        // Chặn bằng package
        List<String> riskPackage = new ArrayList<>();
        riskPackage.add("net.rebeyond.");
        riskPackage.add("com.metasploit.");

        // riskAnnotations
        List<String> riskAnnotations = new ArrayList<>();
        riskAnnotations.add("org.springframework.stereotype.Controller");
        riskAnnotations.add("org.springframework.web.bind.annotation.RestController");
        riskAnnotations.add("org.springframework.web.bind.annotation.RequestMapping");
        riskAnnotations.add("org.springframework.web.bind.annotation.GetMapping");
        riskAnnotations.add("org.springframework.web.bind.annotation.PostMapping");
        riskAnnotations.add("org.springframework.web.bind.annotation.PatchMapping");
        riskAnnotations.add("org.springframework.web.bind.annotation.PutMapping");
        riskAnnotations.add("org.springframework.web.bind.annotation.Mapping");

        // Đầu tiên decompile và kiểm tra từng class
        for (Class<?> clazz : loadedClasses) {
            boolean shouldAdd = false;

            // Kiểm tra superclass
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && riskSuperClassesName.contains(superClass.getName())) {
                shouldAdd = true;
                LogUtils.logit("Found risky superclass in: " + clazz.getName());
            }

            // Kiểm tra package
            for (String packageName : riskPackage) {
                if (clazz.getName().startsWith(packageName)) {
                    shouldAdd = true;
                    LogUtils.logit("Found risky package in: " + clazz.getName());
                    break;
                }
            }

            // Kiểm tra annotations
            if (ClassUtils.isUseAnnotations(clazz, riskAnnotations)) {
                shouldAdd = true;
                LogUtils.logit("Found risky annotation in: " + clazz.getName());
            }

            if (shouldAdd) {
                Decompile(resultClasses, clazz);
            }
        }

        // Định nghĩa các keyword nguy hiểm để kiểm tra nội dung
        List<String> riskKeyword = new ArrayList<>();
        riskKeyword.add("com.metasploit.");
        riskKeyword.add("net.rebeyond.");
        riskKeyword.add("javax.crypto.");
        riskKeyword.add("ProcessBuilder");
        riskKeyword.add("getRuntime");
        riskKeyword.add("shell");
        riskKeyword.add("exec(");

        // Hiển thị kết quả
        String results = "All Suspicious Class    : " + resultClasses.size() + "\n\n";
        String high_level = "============================================================\n" +
                "high risk level Class   : \n";
        String normal_level = "============================================================\n" +
                "normal risk level Class : \n";

        int order = 1;
        for (Class<?> clazz : resultClasses) {
            File dumpPath = PathUtils.getStorePath(clazz, false);
            String level = "normal";
            String content = PathUtils.getFileContent(dumpPath);

            // Kiểm tra các keyword trong nội dung
            for (String keyword : riskKeyword) {
                if (content.contains(keyword)) {
                    level = "high";
                    break;
                }
            }

            // Format kết quả
            String tmp;
            try {
                tmp = String.format(
                        "order       : %d\n" +
                                "name        : %s\n" +
                                "risk level  : %s\n" +
                                "location    : %s\n" +
                                "hashcode    : %s\n" +
                                "classloader : %s\n" +
                                "extends     : %s\n\n",
                        order,
                        clazz.getName(),
                        level,
                        dumpPath.getAbsolutePath(),
                        Integer.toHexString(clazz.hashCode()),
                        clazz.getClassLoader().getClass().getName(),
                        clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "null"
                );
            } catch (NullPointerException e) {
                tmp = String.format(
                        "order       : %d\n" +
                                "name        : %s\n" +
                                "risk level  : %s\n" +
                                "location    : %s\n" +
                                "hashcode    : %s\n" +
                                "classloader : %s\n" +
                                "extends     : [NullPointerException]\n\n",
                        order,
                        clazz.getName(),
                        level,
                        dumpPath.getAbsolutePath(),
                        Integer.toHexString(clazz.hashCode()),
                        clazz.getClassLoader().getClass().getName()
                );
            }

            // Phân loại theo mức độ rủi ro
            if (level.equals("high")) {
                high_level += tmp;
            } else {
                normal_level += tmp;
            }
            order += 1;
        }

        // Ghi kết quả vào log
        LogUtils.result(results + high_level + normal_level);
        LogUtils.logit("Analysis complete. Results written to log file.");
    }
}