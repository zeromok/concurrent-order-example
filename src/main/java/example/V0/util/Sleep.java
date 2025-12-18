package example.V0.util;

public class Sleep {

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("작업 중단됨", e);
		}
	}
}
