package org.example.AddTomcatFilter;

import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author su18
 */
public class DynamicUtils {

	
	public static Class<?> getClass(String classCode) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
		ClassLoader   loader        = Thread.currentThread().getContextClassLoader();
		BASE64Decoder base64Decoder = new BASE64Decoder();
		byte[]        bytes         = base64Decoder.decodeBuffer(classCode);

		Method   method = null;
		Class<?> clz    = loader.getClass();
		while (method == null && clz != Object.class) {
			try {
				method = clz.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
			} catch (NoSuchMethodException ex) {
				clz = clz.getSuperclass();
			}
		}

		if (method != null) {
			method.setAccessible(true);
			return (Class<?>) method.invoke(loader, bytes, 0, bytes.length);
		}

		return null;

	}
}
