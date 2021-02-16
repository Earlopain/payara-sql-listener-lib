package net.c5h8no4na.sqllistener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.jdbc.SQLTraceListener;
import org.glassfish.api.jdbc.SQLTraceRecord;

public class GlassfishSQLTracer implements SQLTraceListener {
	private static final Logger LOG = Logger.getLogger(GlassfishSQLTracer.class.getCanonicalName());

	private static final List<String> PACKAGE_IGNORE_LIST = Arrays.asList("java.lang", "net.c5h8no4na.sqllistener.GlassfishSQLTracer",
			"com.sun", "org.hibernate", "jdk.internal", "org.glassfish", "org.jboss", "org.apache");

	private static final List<String> ALLOWED_METHODS = Arrays.asList("prepareStatement", "executeQuery");

	private static boolean isActive = true;

	public void sqlTrace(SQLTraceRecord record) {
		if (shouldLog(record)) {
			SQLFormatter formatter = new SQLFormatter(record.getParams()[0].toString());
			LOG.info("Classname: " + record.getClassName());
			LOG.info("Methodname:" + record.getMethodName());
			LOG.info("Params: " + formatter.prettyPrint());
			LOG.info("Timestamp: " + record.getTimeStamp());
			LOG.info("Pool" + record.getPoolName());
			LOG.info("Trace: " + getFormattedStackTrace());
		}
	}

	public static void activate() {
		isActive = true;
	}

	public static void deactivate() {
		isActive = false;
	}

	/**
	 * Only log when active and when the method is actually interesting to us.
	 * Hibernate generates a lot of other calls which we don't care about, like
	 * binding parameters or setting client information
	 * 
	 * @param record
	 * @return
	 */
	private boolean shouldLog(SQLTraceRecord record) {
		return isActive && record.getParams() != null && ALLOWED_METHODS.contains(record.getMethodName());
	}

	/**
	 * Gets the current stacktrace, removes all stackframes from this package and
	 * some other noise like com.sun. In the end only stackframes from the calling
	 * project should be left
	 * 
	 * @return The stacktrace newline delimited
	 */
	private String getFormattedStackTrace() {
		List<String> result = new ArrayList<>();
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (StackTraceElement stackTraceElement : stacks) {
			if (PACKAGE_IGNORE_LIST.stream().noneMatch(entry -> stackTraceElement.getClassName().contains(entry))) {
				result.add(stackTraceElement.toString());
			}
		}
		return String.join("\n", result);
	}
}
