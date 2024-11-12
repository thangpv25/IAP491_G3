package org.example.Loader;


import org.example.Utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

import static org.example.Loader.Contraints.JAVA_INTERNAL_PACKAGES;


public class CustomClassLoader extends URLClassLoader {

    private String args;

    private Instrumentation instrumentation;

    private ClassFileTransformer raspClassFileTransformer;

    private File agentFile;


    public CustomClassLoader(final URL url, final ClassLoader classLoader) {
        super(new URL[]{url}, classLoader);
        System.out.println("URL for Custom Loader: " + url.toString());

    }

    public String getArgs() {
        return args;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public ClassFileTransformer getRaspClassFileTransformer() {
        return raspClassFileTransformer;
    }

    public void setRaspClassFileTransformer(ClassFileTransformer raspClassFileTransformer) {
        this.raspClassFileTransformer = raspClassFileTransformer;
    }


    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    public File getAgentFile() {
        return agentFile;
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);

        if (url != null) {
            return url;
        }

        return super.getResource(name);
    }


    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> urls = findResources(name);

        if (urls != null) {
            return urls;
        }

        return super.getResources(name);
    }


    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        System.out.println("CustomLoader Attempting to load class: " + name);
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        if (name.matches(JAVA_INTERNAL_PACKAGES)) {
            return super.loadClass(name, resolve);
        }

        try {
            Class<?> clazz = findClass(name);

            if (resolve) {
                resolveClass(clazz);

            }

            return clazz;
        } catch (Exception e) {
            return super.loadClass(name, resolve);
        }
    }

// load mới class agentCache, viết hàm update agentCache sau đó để match với agent cache hiện tại
    public void loadAgent(File agentFile, String args, Instrumentation inst, AgentCache cache) throws Exception {
        this.args = args;
        this.instrumentation = inst;
        this.agentFile = agentFile;

        this.addURL(agentFile.toURL());
        System.out.println("agentFile.toURL123: " + agentFile.toURL());
        Class<?> bootstrapClass = this.loadClass("org.example.AgentCore.Bootstrap");
//        Class<?> cacheClass = this.loadClass("org.example.Loader.AgentCache");
//        Object agentInstance = cacheClass.getDeclaredConstructor().newInstance();

        System.out.println("bootstrapClass: " + bootstrapClass.getName());
        System.out.println("bootstrapClass classloader is " + bootstrapClass.getClassLoader());
        System.out.println("CustomClassLoader loader: " + this.getClass().getClassLoader());
        URL[] urls = this.getURLs();
        for (URL url : urls) {
            System.out.println("[*] Loaded URL: " + url);
        }
//        System.out.println("Worker class get method: ");
//        for (Method m : workerClass.getDeclaredMethods()) {
//            System.out.print("Method1: " + m.getName() + " Parameters: ");
//            Class<?>[] parameterTypes = m.getParameterTypes();
//            for (Class<?> paramType : parameterTypes) {
//                System.out.print(paramType.getName() + " ");
//            }
//            System.out.println();
//        }
        System.out.println("===========================8386");
        System.out.println("Instrumentation class loader: " + inst.getClass().getClassLoader());
        System.out.println("CustomClassLoader class loader: " + this.getClass().getClassLoader());
        System.out.println("AgentCache class loader: " + cache.getClass().getClassLoader());
        try {
//            bootstrapClass.getMethod("test", Instrumentation.class, AgentCache.class)
//                    .invoke(null, inst,cache);
//            bootstrapClass.getMethod("print", String.class, AgentCache.class).invoke(null,"DUC",cache);
            bootstrapClass.getMethod("print", String.class).invoke(null,"DUC");
            Method test = bootstrapClass.getMethod(
                    "test", Instrumentation.class,  cache.getClass()
            );
            test.invoke(null, instrumentation, cache);

//            bootStrapClass.getMethod(
//                    "bootStrap", Instrumentation.class, SuClassLoader.class, AgentCache.class
//            ).invoke(args, inst, this, cache);
//        }
//            workerClass.getDeclaredMethod(
//                    "print", String.class).invoke(null,"duc");
//            workerClass.getMethod(
//                    "test",Instrumentation.class,  AgentCache.class).invoke(null, inst,  cache);
//            System.out.println("Method123: " + test.getName() + " Parameters: "+test.getParameterTypes());

//            Method method = workerClass.getDeclaredMethod("test",
//                    Class.forName("java.lang.instrument.Instrumentation"),
//                    Class.forName("org.example.Loader.AgentCache", true, this)
//            );
//            Method method = bootstrapClass.getMethod("test", Instrumentation.class);
//            System.out.println("Agent loaded !");
        } catch (NoSuchMethodException e) {
            System.out.println("NoSuchMethodException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        workerClass.getMethod(
//                "test", Instrumentation.class, CustomClassLoader.class, AgentCache.class
//        ).invoke(null, inst, this, cache);
    }


    public boolean closeClassLoader() {
        try {
            Class<?> clazz = URLClassLoader.class;
            Method[] methods = clazz.getMethods();

            for (Method method : methods) {
                if ("close".equals(method.getName())) {
                    method.invoke(this);

                    return true;
                }
            }

            Field ucpField = clazz.getDeclaredField("ucp");
            ucpField.setAccessible(true);
            Object ucp = ucpField.get(this);

            Field loadersField = ucp.getClass().getDeclaredField("loaders");
            loadersField.setAccessible(true);
            List<?> loaders = (List<?>) loadersField.get(ucp);

            for (Object loader : loaders) {
                Class<?> jarLoaderClass = loader.getClass();
                Method method = jarLoaderClass.getDeclaredMethod("getJarFile");
                method.setAccessible(true);

                JarFile jarFile = (JarFile) method.invoke(loader);
                jarFile.close();

                StringUtils.println("Closed Jar: [" + jarFile.getName() + "]");
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
