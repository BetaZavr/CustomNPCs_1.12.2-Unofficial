package noppes.npcs.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomNPCsScheduler {

	private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	public static void runTack(Runnable task) {
		CustomNPCsScheduler.executor.schedule(task, 0L, TimeUnit.MILLISECONDS);
	}

	public static void runTack(Runnable task, int delayMilliSeconds) {
		CustomNPCsScheduler.executor.schedule(task, delayMilliSeconds, TimeUnit.MILLISECONDS);
	}
}
