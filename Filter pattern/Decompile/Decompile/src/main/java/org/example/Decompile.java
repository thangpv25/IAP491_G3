package org.example;

import org.example.common.Decompiler;
import org.example.utils.PathUtils;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class Decompile {
    public static void agentmain(String agentArgs, Instrumentation instrumentation){
        List<Class<?>> resultClasses = new ArrayList<Class<?>>();

        // Nhận tất cả các lớp đã tải và tên lớp
        Class<?>[] loadedClasses = instrumentation.getAllLoadedClasses();
        for (Class<?> cls : loadedClasses) {
            //decompile class ra file
            File dumpJavaFile = PathUtils.getStorePath(cls, false);
            String source = Decompiler.decompile(cls.getName(), null, false);
            PathUtils.writeByteArrayToFile(dumpJavaFile, source.getBytes());
        }
    }
}
