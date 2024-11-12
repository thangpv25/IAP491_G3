package org.example.Loader;


import org.example.Utils.StringUtils;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;

import static org.example.Loader.AgentAttacher.getAgentFileUrl;
import static org.example.Loader.Contraints.AGENT_FILE_NAME;
import static org.example.Loader.Contraints.AGENT_NAME;


public class Agent {
    private static CustomClassLoader customClassLoader;
    private static  final AgentCache AGENT_CACHE = null;


    private static synchronized void setCustomClassLoader(URL jarFile) {
        if (customClassLoader == null) {
            customClassLoader = new CustomClassLoader(jarFile, Agent.class.getClassLoader());
        }
    }
    private static synchronized void setAgentCache() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (customClassLoader != null) {
            Class<?> cacheClass = customClassLoader.loadClass("org.example.Loader.AgentCache");
            AGENT_CACHE = (AgentCache) cacheClass.getDeclaredConstructor().newInstance();
        }
    }

    public static File getAgentFile() throws MalformedURLException {
//        URL tempAgentFileUrl = getAgentFileUrl();
//        String tempString = tempAgentFileUrl.toString();
//        if (tempString.contains("%20")) {
//            tempString = tempString.replace("%20", " ");
//            tempAgentFileUrl = new URL(tempString);
//        }
//        return new File(tempAgentFileUrl.getFile());
        return new File(getAgentFileUrl().getFile());
    }

    private static void detachAgent() {
        synchronized (AGENT_CACHE) {
            Set<ClassFileTransformer> transformers = AGENT_CACHE.getTransformers();
            Instrumentation instrumentation = AGENT_CACHE.getInstrumentation();

            if (instrumentation != null) {
                Class<?>[] loadedClass = instrumentation.getAllLoadedClasses();
                Set<String> reTransformSet = new HashSet<String>();

                reTransformSet.addAll(AGENT_CACHE.getReTransformClass());
                reTransformSet.addAll(AGENT_CACHE.getModifiedClass());

                // 注销已注册的Transformer
                for (Iterator<ClassFileTransformer> iterator = transformers.iterator(); iterator.hasNext(); ) {
                    ClassFileTransformer transformer = iterator.next();
                    instrumentation.removeTransformer(transformer);
                    iterator.remove();
                    System.out.println("Removing Transformer: " + transformer.getClass() + " Success");
                }

                // 恢复所有已经被 suagent reTransform、modified的类
                for (Class<?> clazz : loadedClass) {
                    for (Iterator<String> iterator = reTransformSet.iterator(); iterator.hasNext(); ) {
                        String className = iterator.next();

                        if (clazz.getName().equals(className) && instrumentation.isModifiableClass(clazz)) {
                            try {
                                instrumentation.retransformClasses(clazz);
                                iterator.remove();

                                System.out.println("ReTransform " + clazz);
                            } catch (UnmodifiableClassException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                // 清空缓存的Agent对象
                AGENT_CACHE.clear();
            }

            // 关闭RASP 类加载器
            if (customClassLoader != null && customClassLoader.closeClassLoader()) {
                System.out.println("Release SuAgent Resource Success");
                customClassLoader = null;
            }

            System.out.println("Detach Success");
        }
    }

    private static void initiateAgent(final String arg, final Instrumentation inst) {
        String[] args = arg != null ? arg.split("\\s+") : new String[0];

        synchronized (AGENT_CACHE) {
            try {
                if (args.length > 0) {
                    if ("detach".equalsIgnoreCase(args[0])) {
                        detachAgent();
                        return;
                    } else if ("attach".equalsIgnoreCase(args[0]) && AGENT_CACHE.getInstrumentation() != null) {
                        StringUtils.println(AGENT_NAME + "Already injected!");
                        return;
                    }
                }
//
//                File loaderFile = getAgentFile();
//                File agentFile = getScannerJarFileUrl(loaderFile);
//                URL agentFileUrl = agentFile.toURI().toURL();
                File agentFile = getAgentFile();
                URL agentFileUrl = agentFile.toURI().toURL();

//                agentFileUrl= new URL(tempURL);
                System.out.println("agentFile: " + agentFile);
                System.out.println("getAgentFileUrl(): " + agentFileUrl);

//                System.out.println("agentFileUrl: " + agentFileUrl);
                setCustomClassLoader(agentFileUrl);
                setAgentCache();
                AGENT_CACHE.setInstrumentation(inst);

                inst.appendToBootstrapClassLoaderSearch(new JarFile(agentFile));

                customClassLoader.loadAgent(agentFile, arg, inst, AGENT_CACHE);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }


    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Premain executed: Test Agent attached.");
        if (!inst.isRetransformClassesSupported()) {
            System.out.println("Class retransformation is not supported.");
            return;
        }
        initiateAgent(agentArgs, inst);

    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("Agentmain executed: Test Agent attached.");
        System.out.println("This agent class loader: " + Agent.class.getClassLoader());
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("Ctx Loader: " + contextLoader);
        if (!inst.isRetransformClassesSupported()) {
            System.out.println("Class retransformation is not supported.");
            return;
        }
        initiateAgent(agentArgs, inst);
//             printLoadedClass(inst);

    }

    public static void printLoadedClass(Instrumentation inst) {
        System.out.println("All loaded classes: ");
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (clazz != null) {
                try {
                    // Get class
//                    if (clazz.getPackage().toString().contains("org.example")) {
//                        String className = clazz.toString();
//                        System.out.println(className);
//                    }
                    System.out.println("Class: " + clazz.getName() + ", Class Loader: " + clazz.getClassLoader());
                } catch (Exception e) {
                    System.err.println("Error finding class: " + e.getMessage());
                }
//
            }
        }

    }


}