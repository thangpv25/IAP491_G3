package target.taint.AddController;

import java.util.Base64;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author su18
 */
public class DynamicUtils {


	public static String CONTROLLER_CLASS_STRING =
			"yv66vgAAADQALQoABgAeCwAfACAIACEKACIAIwcAJAcAJQEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyV" +
			"GFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQAvTG9yZy9zdTE4L21lbXNoZWxsL3NwcmluZy9vdGhlci9UZXN" +
			"0Q29udHJvbGxlcjsBAAVpbmRleAEAUihMamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVxdWVzdDtMamF2YXgvc" +
			"2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVzcG9uc2U7KVYBAAdyZXF1ZXN0AQAnTGphdmF4L3NlcnZsZXQvaHR0cC9IdHR" +
			"wU2VydmxldFJlcXVlc3Q7AQAIcmVzcG9uc2UBAChMamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVzcG9uc2U7A" +
			"QAKRXhjZXB0aW9ucwcAJgEAGVJ1bnRpbWVWaXNpYmxlQW5ub3RhdGlvbnMBADRMb3JnL3NwcmluZ2ZyYW1ld29yay93ZWI" +
			"vYmluZC9hbm5vdGF0aW9uL0dldE1hcHBpbmc7AQAKU291cmNlRmlsZQEAE1Rlc3RDb250cm9sbGVyLmphdmEBACtMb3JnL" +
			"3NwcmluZ2ZyYW1ld29yay9zdGVyZW90eXBlL0NvbnRyb2xsZXI7AQA4TG9yZy9zcHJpbmdmcmFtZXdvcmsvd2ViL2JpbmQ" +
			"vYW5ub3RhdGlvbi9SZXF1ZXN0TWFwcGluZzsBAAV2YWx1ZQEABS9zdTE4DAAHAAgHACcMACgAKQEADXN1MTggaXMgaGVyZ" +
			"X4HACoMACsALAEALW9yZy9zdTE4L21lbXNoZWxsL3NwcmluZy9vdGhlci9UZXN0Q29udHJvbGxlcgEAEGphdmEvbGFuZy9" +
			"PYmplY3QBABNqYXZhL2xhbmcvRXhjZXB0aW9uAQAmamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVzcG9uc2UBA" +
			"AlnZXRXcml0ZXIBABcoKUxqYXZhL2lvL1ByaW50V3JpdGVyOwEAE2phdmEvaW8vUHJpbnRXcml0ZXIBAAdwcmludGxuAQA" +
			"VKExqYXZhL2xhbmcvU3RyaW5nOylWACEABQAGAAAAAAACAAEABwAIAAEACQAAAC8AAQABAAAABSq3AAGxAAAAAgAKAAAAB" +
			"gABAAAAEQALAAAADAABAAAABQAMAA0AAAABAA4ADwADAAkAAABOAAIAAwAAAAwsuQACAQASA7YABLEAAAACAAoAAAAKAAI" +
			"AAAAVAAsAFgALAAAAIAADAAAADAAMAA0AAAAAAAwAEAARAAEAAAAMABIAEwACABQAAAAEAAEAFQAWAAAABgABABcAAAACA" +
			"BgAAAACABkAFgAAABIAAgAaAAAAGwABABxbAAFzAB0=";

	public static String INTERCEPTOR_CLASS_STRING =
			"yv66vgAAADQAKwoABgAbCwAcAB0IAB4KAB8AIAcAIQcAIgcAIwEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYm" +
			"VyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQAwTG9yZy9zdTE4L21lbXNoZWxsL3NwcmluZy9vdGhlci9U" +
			"ZXN0SW50ZXJjZXB0b3I7AQAJcHJlSGFuZGxlAQBkKExqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXF1ZXN0O0" +
			"xqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXNwb25zZTtMamF2YS9sYW5nL09iamVjdDspWgEAB3JlcXVlc3QB" +
			"ACdMamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVxdWVzdDsBAAhyZXNwb25zZQEAKExqYXZheC9zZXJ2bGV0L2" +
			"h0dHAvSHR0cFNlcnZsZXRSZXNwb25zZTsBAAdoYW5kbGVyAQASTGphdmEvbGFuZy9PYmplY3Q7AQAKRXhjZXB0aW9ucwcA" +
			"JAEAClNvdXJjZUZpbGUBABRUZXN0SW50ZXJjZXB0b3IuamF2YQwACAAJBwAlDAAmACcBABBpJ20gaW50ZXJjZXB0b3J+Bw" +
			"AoDAApACoBAC5vcmcvc3UxOC9tZW1zaGVsbC9zcHJpbmcvb3RoZXIvVGVzdEludGVyY2VwdG9yAQAQamF2YS9sYW5nL09i" +
			"amVjdAEAMm9yZy9zcHJpbmdmcmFtZXdvcmsvd2ViL3NlcnZsZXQvSGFuZGxlckludGVyY2VwdG9yAQATamF2YS9sYW5nL0" +
			"V4Y2VwdGlvbgEAJmphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlc3BvbnNlAQAJZ2V0V3JpdGVyAQAXKClMamF2" +
			"YS9pby9QcmludFdyaXRlcjsBABNqYXZhL2lvL1ByaW50V3JpdGVyAQAHcHJpbnRsbgEAFShMamF2YS9sYW5nL1N0cmluZz" +
			"spVgAhAAUABgABAAcAAAACAAEACAAJAAEACgAAAC8AAQABAAAABSq3AAGxAAAAAgALAAAABgABAAAACwAMAAAADAABAAAA" +
			"BQANAA4AAAABAA8AEAACAAoAAABZAAIABAAAAA0suQACAQASA7YABASsAAAAAgALAAAACgACAAAADwALABAADAAAACoABA" +
			"AAAA0ADQAOAAAAAAANABEAEgABAAAADQATABQAAgAAAA0AFQAWAAMAFwAAAAQAAQAYAAEAGQAAAAIAGg==";

	public static Class<?> getClass(String classCode) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
		ClassLoader   loader        = Thread.currentThread().getContextClassLoader();
		byte[] bytes = Base64.getDecoder().decode(classCode);

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
