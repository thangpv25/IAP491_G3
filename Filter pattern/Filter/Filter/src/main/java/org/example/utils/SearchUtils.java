package org.example.utils;

import org.example.utils.matcher.Matcher;
import org.example.utils.matcher.RegexMatcher;
import org.example.utils.matcher.WildcardMatcher;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * referer https://github.com/alibaba/arthas/blob/master/core/src/main/java/com/taobao/arthas/core/util/SearchUtils.java
 *
 */

public class SearchUtils {

    public static Set<Class<?>> searchClass(Instrumentation inst, Matcher<String> classNameMatcher, int limit) {
        if (classNameMatcher == null) {
            return Collections.emptySet();
        }
        final Set<Class<?>> matches = new HashSet<Class<?>>();
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (classNameMatcher.matching(clazz.getName())) {
                matches.add(clazz);
            }
            if (matches.size() >= limit) {
                break;
            }
        }
        return matches;
    }

    public static Set<Class<?>> searchClass(Instrumentation inst, Matcher<String> classNameMatcher) {
        return searchClass(inst, classNameMatcher, Integer.MAX_VALUE);
    }

    public static Set<Class<?>> searchClass(Instrumentation inst, String classPattern, boolean isRegEx) {
        boolean isDisableSubClass = true;
        Matcher<String> classNameMatcher = classNameMatcher(classPattern, isRegEx);
        return isDisableSubClass ? searchClass(inst, classNameMatcher) :
                searchSubClass(inst, searchClass(inst, classNameMatcher));
    }

    public static Set<Class<?>> searchClass(Instrumentation inst, String classPattern, boolean isRegEx, String code) {
        Set<Class<?>> matchedClasses = searchClass(inst, classPattern, isRegEx);
        return filter(matchedClasses, code);
    }

    public static Set<Class<?>> searchClassOnly(Instrumentation inst, String classPattern, boolean isRegEx) {
        Matcher<String> classNameMatcher = classNameMatcher(classPattern, isRegEx);
        return searchClass(inst, classNameMatcher);
    }

    public static Set<Class<?>> searchClassOnly(Instrumentation inst, String classPattern, int limit) {
        Matcher<String> classNameMatcher = classNameMatcher(classPattern, false);
        return searchClass(inst, classNameMatcher, limit);
    }

    public static Set<Class<?>> searchClassOnly(Instrumentation inst, String classPattern, boolean isRegEx, String code) {
        Set<Class<?>> matchedClasses = searchClassOnly(inst, classPattern, isRegEx);
        return filter(matchedClasses, code);
    }

    private static Set<Class<?>> filter(Set<Class<?>> matchedClasses, String code) {
        if (code == null) {
            return matchedClasses;
        }

        Set<Class<?>> result = new HashSet<Class<?>>();
        if (matchedClasses != null) {
            for (Class<?> c : matchedClasses) {
                if (Integer.toHexString(c.getClassLoader().hashCode()).equals(code)) {
                    result.add(c);
                }
            }
        }
        return result;
    }

    public static Matcher<String> classNameMatcher(String classPattern, boolean isRegEx) {
        if (StringUtils.isEmpty(classPattern)) {
            classPattern = isRegEx ? ".*" : "*";
        }
        if (!classPattern.contains("$$Lambda")) {
            classPattern = StringUtils.replace(classPattern, "/", ".");
        }
        return isRegEx ? new RegexMatcher(classPattern) : new WildcardMatcher(classPattern);
    }

    /**
     * 搜索目标类的子类
     *
     * @param inst     inst
     * @param classSet 当前类集合
     * @return 匹配的子类集合
     */
    public static Set<Class<?>> searchSubClass(Instrumentation inst, Set<Class<?>> classSet) {
        final Set<Class<?>> matches = new HashSet<Class<?>>();
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            for (Class<?> superClass : classSet) {
                if (superClass.isAssignableFrom(clazz)) {
                    matches.add(clazz);
                    break;
                }
            }
        }
        return matches;
    }


    /**
     * 搜索目标类的内部类
     *
     * @param inst inst
     * @param c    当前类
     * @return 匹配的类的集合
     */
    public static Set<Class<?>> searchInnerClass(Instrumentation inst, Class<?> c) {
        final Set<Class<?>> matches = new HashSet<Class<?>>();
        for (Class<?> clazz : inst.getInitiatedClasses(c.getClassLoader())) {
            if (c.getClassLoader() != null && clazz.getClassLoader() != null && c.getClassLoader().equals(clazz.getClassLoader())) {
                if (clazz.getName().startsWith(c.getName())) {
                    matches.add(clazz);
                }
            }
        }
        return matches;
    }
}