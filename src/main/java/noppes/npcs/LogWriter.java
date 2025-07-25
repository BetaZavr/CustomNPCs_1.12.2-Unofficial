package noppes.npcs;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class LogWriter {

	private static void log(Level level, Object msg) {
		StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
		String methodName = caller.getMethodName() + "()";
		if (methodName.startsWith("lambda$")) {
			methodName = methodName.substring(methodName.indexOf("$") + 1);
			if (methodName.contains("$")) {
				methodName = methodName.substring(0, methodName.indexOf("$"));
			}
		}
		if (caller.getClassName().equals("noppes.npcs.util.DataDebug")) {
			LogManager.getLogger(caller.getClassName()).log(level, "{}", msg);
		} else {
			LogManager.getLogger(caller.getClassName()).log(level, "{}(Line#{}): {}", methodName, caller.getLineNumber(), msg);
		}
	}

	public static void debug(String msg) {
		if (!CustomNpcs.VerboseDebug || msg == null || msg.trim().isEmpty()) { return; }
		log(Level.INFO, msg);
	}

	public static void error(Object msg) {
		if (msg == null || msg.toString().isEmpty()) { return; }
		log(Level.ERROR, msg);
	}

	public static void error(Object msg, Throwable e) {
		if (msg != null && !msg.toString().isEmpty()) { log(Level.ERROR, msg.toString()); }
		if (e != null) { log(Level.ERROR, e); }
	}

	public static void except(Throwable e) {
		if (e == null) { return; }
		log(Level.FATAL, e);
	}

	public static void info(Object msg) {
		if (msg == null || msg.toString().isEmpty()) { return; }
		log(Level.INFO, msg);
	}

	public static void warn(Object msg) {
		if (msg == null || msg.toString().isEmpty()) { return; }
		log(Level.WARN, msg);
	}

}
