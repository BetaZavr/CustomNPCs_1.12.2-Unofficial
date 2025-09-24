package noppes.npcs;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		Logger logger = LogManager.getLogger(caller.getClassName());
		String clss = caller.getClassName();
		if (clss.contains(".")) { clss = clss.substring(clss.lastIndexOf(".") + 1); }
		String message;
		if (caller.getClassName().contains(".DataDebug")) { message = msg.toString(); }
		else { message = "(" + clss + ".java:" + caller.getLineNumber() + ") " + methodName + " \"" + msg + "\""; }
		logger.log(level, message);
	}

	public static void debug(String msg) {
		if (!CustomNpcs.VerboseDebug || msg == null || msg.trim().isEmpty()) { return; }
		log(Level.INFO, msg);
	}

	public static void error(Object msg) {
		if (msg == null || msg.toString().isEmpty()) { return; }
		if (msg instanceof Throwable) {
			StringBuilder message = new StringBuilder(" \"").append(msg).append("\":");
			for (StackTraceElement traceElement : ((Throwable) msg).getStackTrace()) { message.append("\n\tat ").append(traceElement); }
			log(Level.ERROR, message.toString());
		}
		else { log(Level.ERROR, msg); }
	}

	public static void error(Object msg, Throwable e) {
		StringBuilder message = new StringBuilder();
		if (msg != null && !msg.toString().isEmpty()) { message = new StringBuilder(msg.toString()); }
		if (e != null) {
			message.append(" \"").append(e).append("\":");
			for (StackTraceElement traceElement : e.getStackTrace()) { message.append("\n\tat ").append(traceElement); }
		}
		log(Level.ERROR, message.toString());
	}

	public static void except(Throwable e) {
		if (e == null) { return; }
		StringBuilder message = new StringBuilder(e.toString());
		message.append(" \"").append(e).append("\":");
		for (StackTraceElement traceElement : e.getStackTrace()) { message.append("\n\tat ").append(traceElement); }
		log(Level.FATAL, message.toString());
	}

	@SuppressWarnings("all")
	public static void pathInfo(Object msg, int maxLines) {
		if (msg == null || msg.toString().isEmpty()) { return; }
		StringBuilder message = new StringBuilder(msg + ":");
		StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
		for (int i = 2; i < stackTraces.length && (maxLines < 1 || i < maxLines + 2); i++) { message.append("\n\tat ").append(stackTraces[i]); }
		log(Level.INFO, message.toString());
	}

	public static void info(Object msg) {
		if (msg == null || msg.toString().isEmpty()) { return; }
		log(Level.INFO, msg);
	}

	public static void warn(Object msg) {
		if (msg == null || msg.toString().isEmpty()) { return; }
		StringBuilder message = new StringBuilder(msg + ":");
		StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
		for (int i = 2; i < stackTraces.length && i < 7; i++) { message.append("\n\tat ").append(stackTraces[i]); }
		log(Level.WARN, message.toString());
	}

}
