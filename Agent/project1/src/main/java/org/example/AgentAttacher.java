package org.example;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class AgentAttacher {
    public static void main(String[] args) {
        String agentPath = "D:\\IntelliJ project\\project1\\target\\classes\\org\\example\\testagent.jar";
//        String agentPath = "D:\\IntelliJ project\\WebTomcat\\src\\main\\java\\org\\example\\WEB-INF\\testagent.jar";

        String targetPid= "28328"; //"args[0]";
//      Load vao PID cua JVM da dinh san
        try {

            VirtualMachine vm = VirtualMachine.attach(targetPid);  // VirtualMachine vm = VirtualMachine.attach(targetPid);
            // Load agent
            vm.loadAgent(agentPath);
            vm.detach();
            System.out.println("Agent attached successfully to JVM with PID: " + targetPid);
        } catch (Exception e) {
            System.err.println("Failed to attach to JVM with PID: " + targetPid);
            e.printStackTrace();
        }

//       Search PID cua JVM (chua xong, thuc hien loi)
//        for (VirtualMachineDescriptor vmd : VirtualMachine.list()) {
//            System.out.println("PID: " + vmd.id() + ", Main class: " + vmd.displayName());
//            // Target vào PID nhất định: String targetPid = "";
//            try {
//                // Attach vào tất cả JVM
//                VirtualMachine vm = VirtualMachine.attach(vmd.id());  // VirtualMachine vm = VirtualMachine.attach(targetPid);
//                // Load agent
//                vm.loadAgent(agentPath);
//                vm.detach();
//                System.out.println("Agent attached successfully to JVM with PID: " + vmd.id());
//            } catch (Exception e) {
//                System.err.println("Failed to attach to JVM with PID: " + vmd.id());
//                e.printStackTrace();
//            }
//        }
    }
}