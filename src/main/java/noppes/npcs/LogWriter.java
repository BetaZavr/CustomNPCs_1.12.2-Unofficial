package noppes.npcs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class LogWriter {

	private final static SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
	private static Handler handler;
	private static final Logger logger = Logger.getLogger("CustomNPCs");

	static {
		try {
			File dir = new File("logs");
			boolean bo = false;
			if (dir.exists() || !dir.mkdir()) {File file = new File(dir, "CustomNPCs-latest.log");
				File lock = new File(dir, "CustomNPCs-latest.log.lck");
				File file2 = new File(dir, "CustomNPCs-1.log");
				File file3 = new File(dir, "CustomNPCs-2.log");
				File file4 = new File(dir, "CustomNPCs-3.log");
				if (lock.exists()) {
					bo = lock.delete();
				}
				if (file4.exists()) {
					bo = file4.delete();
				}
				if (file3.exists()) {
					bo = file3.renameTo(file4);
				}
				if (file2.exists()) {
					bo = file2.renameTo(file3);
				}
				if (file.exists()) {
					bo = file.renameTo(file2);
				}
				FileOutputStream outStream = new FileOutputStream(file);
				(LogWriter.handler = new StreamHandler(outStream, new Formatter() {
					@Override
					public String format(LogRecord record) {
						StackTraceElement element = Thread.currentThread().getStackTrace()[8];
						String line = "[" + element.getClassName() + ":" + element.getLineNumber() + "] ";
						String time = "[" + LogWriter.dateformat.format(new Date(record.getMillis())) + "][" + record.getLevel() + "/" + "CustomNPCs" + "]" + line;
						if (record.getThrown() != null) {
							StringWriter sw = new StringWriter();
							PrintWriter pw = new PrintWriter(sw);
							record.getThrown().printStackTrace(pw);
							return time + sw;
						}
						return time + record.getMessage() + System.lineSeparator();
					}
				})).setLevel(Level.ALL);
				LogWriter.logger.addHandler(LogWriter.handler);
				LogWriter.logger.setUseParentHandlers(false);
				Handler consoleHandler = new ConsoleHandler();
				consoleHandler.setFormatter(LogWriter.handler.getFormatter());
				consoleHandler.setLevel(Level.ALL);
				LogWriter.logger.addHandler(consoleHandler);
				LogWriter.logger.setLevel(Level.ALL);
				info(new Date().toString());
				if (bo) { LogWriter.logger.log(Level.INFO, "Logger done.");}
			}
		} catch (Exception e) {
			LogWriter.logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static void debug(String msg) {
		if (!CustomNpcs.VerboseDebug) {
			return;
		}
		LogWriter.logger.log(Level.INFO, msg);
		LogWriter.handler.flush();
	}

	public static void error(Object msg) {
		LogWriter.logger.log(Level.WARNING, msg.toString());
	}

	public static void error(Object msg, Throwable e) {
		LogWriter.logger.log(Level.WARNING, msg.toString());
		LogWriter.logger.log(Level.WARNING, e.getMessage(), e);
		LogWriter.handler.flush();
	}

	public static void except(Throwable e) {
		LogWriter.logger.log(Level.SEVERE, e.getMessage(), e);
		LogWriter.handler.flush();
	}

	public static void info(Object msg) {
		LogWriter.logger.log(Level.FINE, msg.toString());
		LogWriter.handler.flush();
	}

	public static void warn(Object msg) {
		LogWriter.logger.log(Level.WARNING, msg.toString());
		LogWriter.handler.flush();
	}
}
