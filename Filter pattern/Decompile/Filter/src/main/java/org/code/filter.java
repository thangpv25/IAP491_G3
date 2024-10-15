package org.code;

import org.code.common.Decompiler;
import org.code.utils.ClassUtils;
import org.code.utils.LogUtils;
import org.code.utils.PathUtils;
import org.code.utils.SearchUtils;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class filter {
    public static File agent_work_directory = null;

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        catchThief(agentArgs, instrumentation);
        instrumentation.addTransformer(new DefineTransformer(), true);
    }


    static class DefineTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            return classfileBuffer;
        }
    }

    private static synchronized void catchThief(String name, Instrumentation ins) {
        LogUtils.logit("Agent.jar is success attached");
        LogUtils.logit("Current Agent.jar Directory : " + PathUtils.getCurrentDirectory());
        LogUtils.logit("Prepared Dump class name    : " + name);

        List<Class<?>> resultClasses = new ArrayList<Class<?>>();

        // Nhận tất cả các lớp đã tải và tên lớp
        Class<?>[] loadedClasses = ins.getAllLoadedClasses();
        LogUtils.logit("Found All Loaded Classes    : " + loadedClasses.length);
        List<String> loadedClassesNames = new ArrayList<String>();
        for (Class<?> cls : loadedClasses) {
            loadedClassesNames.add(cls.getName());
            File dumpJavaFile = PathUtils.getStorePath(cls, false);
            //ClassUtils.dumpClass(ins, cls.getName(), false, Integer.toHexString(cls.getClassLoader().hashCode()));
            String source = Decompiler.decompile(cls.getName(), null, false);
            PathUtils.writeByteArrayToFile(dumpJavaFile, source.getBytes());
            LogUtils.logit("Store java: " + cls.getName() + " to " + dumpJavaFile.getAbsolutePath());


            // Lưu riêng biệt tên lớp đã tải và thông tin hashcode, classloader, hashcode của classloader
            agent_work_directory = new File(PathUtils.getCurrentDirectory());
            File allLoadedClassFile = new File(new File(agent_work_directory, "logs"), "allLoadedClasses.txt");
            LogUtils.logit("Prepared Store All Loaded Classes Name ...");
            PathUtils.appendTextToFile(allLoadedClassFile, "[*] Format: [classname | class-hashcode | classloader | classloader-hashcode]\n");
            ClassUtils.storeAllLoadedClassesName(allLoadedClassFile, loadedClasses);
            LogUtils.logit("All Loaded Classes Name Store in : " + allLoadedClassFile.getAbsolutePath());

            //Dang Dev tiep filter theo blacklist

        }
    }
}