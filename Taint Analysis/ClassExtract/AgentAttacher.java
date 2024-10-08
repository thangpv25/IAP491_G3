import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class AgentAttacher {
    public static void main(String[] args) {
        String agentPath = "target/class-extractor-agent-1.0-SNAPSHOT.jar";


        for (VirtualMachineDescriptor vmd : VirtualMachine.list()) {
            System.out.println("PID: " + vmd.id() + ", Main class: " + vmd.displayName());
            // Target vào PID nhất định: String targetPid = "";
            try {
                // Attach vào tất cả JVM
                VirtualMachine vm = VirtualMachine.attach(vmd.id());  // VirtualMachine vm = VirtualMachine.attach(targetPid);
                // Load agent
                vm.loadAgent(agentPath);
                vm.detach();
                System.out.println("Agent attached successfully to JVM with PID: " + vmd.id());
            } catch (Exception e) {
                System.err.println("Failed to attach to JVM with PID: " + vmd.id());
                e.printStackTrace();
            }
        }
    }
}