import com.sun.tools.attach.VirtualMachine;

public class ExternalHeapDumper {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ExternalHeapDumper <PID>");
            return;
        }

        String pid = args[0];
        String filePath = "E:\\heapdump-clean3.hprof";

        try {
            System.out.println("🔗 Attaching to process " + pid + "...");
            VirtualMachine vm = VirtualMachine.attach(pid);

            // Just attach to wake it up if necessary
            vm.startLocalManagementAgent();
            vm.detach();

            // Now trigger dump via jcmd
            System.out.println("📊 Triggering heap dump via jcmd...");
            Process process = new ProcessBuilder("jcmd", pid, "GC.heap_dump", filePath)
                    .inheritIO()
                    .start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("✅ Heap dump completed: " + filePath);
            } else {
                System.err.println("❌ jcmd failed with exit code: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("❌ Error during heap dump:");
            e.printStackTrace();
        }
    }
}
