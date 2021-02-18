package net.c5h8no4na.sqllistener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.glassfish.api.jdbc.SQLTraceListener;
import org.glassfish.api.jdbc.SQLTraceRecord;

public class GlassfishSQLTracer implements SQLTraceListener {
	private static final List<String> PACKAGE_IGNORE_LIST = Arrays.asList("java.util", "java.lang",
			"net.c5h8no4na.sqllistener.GlassfishSQLTracer", "com.sun", "org.hibernate", "jdk.internal", "org.glassfish", "org.jboss",
			"org.apache");

	private static final List<String> ALLOWED_METHODS = Arrays.asList("prepareStatement", "executeQuery");

	private static final Map<String, SQLInfoStructure> executedQueries = new ConcurrentHashMap<>();

	private static final Map<String, Consumer<SingleSQLQuery>> listeners = new ConcurrentHashMap<>();

	private static boolean isActive = true;

	public void sqlTrace(SQLTraceRecord record) {
		if (shouldLog(record)) {
			SQLFormatter formatter = new SQLFormatter(record.getParams()[0].toString());
			SQLInfoStructure infos = executedQueries.computeIfAbsent(record.getPoolName(), key -> new SQLInfoStructure(key));
			String sql = formatter.prettyPrint();
			List<String> stackTrace = getFilteredStackTrace();
			infos.addQuery(sql, stackTrace, record.getTimeStamp(), record.getExecutionTime());

			SingleSQLQuery query = new SingleSQLQuery();
			query.setPoolName(record.getPoolName());
			query.setSql(sql);
			query.setStackTrace(stackTrace);
			query.setTimestamp(record.getTimeStamp());
			query.setExecutionTime(record.getExecutionTime());

			for (Consumer<SingleSQLQuery> consumer : listeners.values()) {
				consumer.accept(query);
			}

		}
	}

	public static void toggle() {
		isActive = !isActive;
	}

	public static boolean isActive() {
		return isActive;
	}

	public static void clear() {
		executedQueries.clear();
	}

	public static List<SingleSQLQuery> getAll() {
		List<SingleSQLQuery> result = new ArrayList<>();

		for (Entry<String, SQLInfoStructure> a : executedQueries.entrySet()) {
			String poolName = a.getValue().getPoolName();

			for (Entry<String, SQLQuery> b : a.getValue().getQueries().entrySet()) {
				String sql = b.getValue().getSql();

				for (ExecutedSQLInfos c : b.getValue().getInfos()) {
					SingleSQLQuery q = new SingleSQLQuery();
					q.setPoolName(poolName);
					q.setSql(sql);
					q.setStackTrace(c.getStackTrace());
					q.setTimestamp(c.getTimestamp());
					q.setExecutionTime(c.getExecutionTime());
					result.add(q);
				}
			}
		}

		return result;
	}

	public static void addListener(String id, Consumer<SingleSQLQuery> consumer) {
		listeners.put(id, consumer);
	}

	public static void removeListener(String id) {
		listeners.remove(id);
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
	private List<String> getFilteredStackTrace() {
		List<String> result = new ArrayList<>();
		for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
			if (shouldAddStackTraceElement(stackTraceElement)) {
				result.add(stackTraceElement.toString());
			}
		}
		return result;
	}

	private boolean shouldAddStackTraceElement(StackTraceElement stackTraceElement) {
		return PACKAGE_IGNORE_LIST.stream().noneMatch(entry -> {
			return stackTraceElement.getClassName().contains(entry);
		});
	}

}
