package param;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

public class Timer {
	public static <T> T time(Callable<T> t, String name) {
		long start = System.currentTimeMillis();
		T result;
		try {
			result = t.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		System.out.println(MessageFormat.format("{0} took {1} milliseconds", name, (System.currentTimeMillis() - start)));
		return result;
	}
	
	public static void time(Runnable t, String name) {
		long start = System.currentTimeMillis();
		t.run();
		System.out.println(MessageFormat.format("{0} took {1} milliseconds", name, (System.currentTimeMillis() - start)));
	}
}