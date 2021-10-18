package sample.utils;

public class Utils {

	private Utils() {
	}

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static int getThreadNumber(Thread thread) {
		return Integer.parseInt(thread.getName().substring(thread.getName().lastIndexOf('-') + 1));
	}
}
