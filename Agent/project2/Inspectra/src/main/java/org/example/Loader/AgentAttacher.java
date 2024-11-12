package org.example.Loader;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.example.Utils.StringUtils.printProcessList;
import static org.example.Utils.StringUtils.printUsage;


public class AgentAttacher {

    private static final Pattern JAVA_VERSION_PATTERN = Pattern.compile("^1\\.[0-5]");
    public static URL getAgentFileUrl() {
        return Agent.class.getProtectionDomain().getCodeSource().getLocation();
    }

    public static void attachJvm(String processId, String args, VMProxy vmLoader) {
        try {

            Object vm = vmLoader.attach(processId);
            System.out.println("loaderFileUrl.toURI()).getAbsolutePath(): ");
            vmLoader.loadAgent(vm, new File(getAgentFileUrl().toURI()).getAbsolutePath(), args);
            vmLoader.detach(vm);
        } catch (Exception e) {
            System.out.println("Attach To JVM Exception: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        VMProxy vmProxy = new VMProxy();
        if (args.length == 0) {
            printUsage();
            return;
        }

        // 判断当前 JDK 版本
//        String javaVersion = System.getProperty("java.version");
//        if (JAVA_VERSION_PATTERN.matches(javaVersion).find()) {
//            System.err.println("JDK Version: " + javaVersion + ". JDK Version Can Not Less Than 1.6!");
//        }

        if ("attach".equalsIgnoreCase(args[0]) || "detach".equalsIgnoreCase(args[0])) {
//            attachJvm(args[1].trim(), args[0], vmProxy);
            attachJvm(autoLoadingJVM(vmProxy), args[0], vmProxy);
        } else if ("list".equalsIgnoreCase(args[0])) {
            printProcessList(vmProxy.listJvmPid());
        } else {
            printUsage();
    }

//    String agentPath = "D:\\IntelliJ project\\project1\\target\\classes\\org\\example\\testagent.jar";
//        String targetPid = null;
//        Map<String, String> processMap = listJvmPid();
//                try {
//                    for (String processId : processMap.keySet()) {
//                        String processDisplayName = processMap.get(processId);
//                        System.out.println("PID:" + processId + "\tProcessName:" + ("".equals(processDisplayName) ? "NONE" : processDisplayName));
//                        if (processDisplayName.toLowerCase().contains(".bootstrap")) {
//                            targetPid = processId;
//                        }
//                    }
//                    VirtualMachine vm = VirtualMachine.attach(targetPid);
//                    // Load agent
//                    vm.loadAgent(agentPath);
//                    vm.detach();
//                    System.out.println("Agent attached successfully to JVM with PID: " + targetPid);
//
//                } catch (Exception e) {
//                    System.err.println("Failed to attach to JVM with PID: " + targetPid);
//                    e.printStackTrace();
//                }
            }

    public static Map<String, String> getProcessList() {
        Map<String, String> processMap = new HashMap<String, String>();
        for (VirtualMachineDescriptor vmDescriptor : VirtualMachine.list()) {
            String displayName = vmDescriptor.displayName();
            String targetPid = vmDescriptor.id();
            processMap.put(vmDescriptor.id(),vmDescriptor.displayName());
        }
        return processMap;
    }
    private static String autoLoadingJVM(VMProxy loader) {
        try {
            Map<String, String> processMap = loader.listJvmPid();

            for (String processId : processMap.keySet()) {
                String name = processMap.get(processId).toLowerCase();
                System.out.println("PID:" + processId + "\tProcessName:" + ("".equals(name) ? "NONE" : name));
                if (name.contains("bootstrap")){
                    return processId;

                }
            }
        } catch (Exception e) {
            System.out.println("Load JVM PID Exception:" + e);
            e.printStackTrace();
        }
        return "";
    }
}
