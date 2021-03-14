package net.c5h8no4na.sqllistener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.glassfish.api.jdbc.SQLTraceListener;
import org.glassfish.api.jdbc.SQLTraceRecord;

import net.c5h8no4na.sqllistener.formatter.BinaryData;

public class GlassfishSQLTracer implements SQLTraceListener {

	private static final Integer LAST_QUERIES_MAX_SIZE = 20;

	private static final List<String> PACKAGE_IGNORE_LIST = List.of("java.util", "java.lang",
			"net.c5h8no4na.sqllistener.GlassfishSQLTracer", "com.sun", "org.hibernate", "jdk.internal", "org.glassfish", "org.jboss",
			"org.apache", "sun.reflect", "java.security", "javax.security");

	private static final List<String> BINARY_STATEMENT_SETTERS = List.of("setAsciiStream", "setBinaryStream", "setBlob", "setBytes",
			"setCharacterStream", "setClob", "setNCharacterStream", "setNClob");

	private static final List<String> NON_BINARY_STATEMENT_SETTERS = List.of("setBigDecimal", "setBoolean", "setByte", "setDate",
			"setDouble", "setFloat", "setInt", "setLong", "setNString", "setString", "setShort", "setTime", "setTimestamp");

	private static final List<String> SEND_QUERY_TO_SERVER = List.of("execute", "executeQuery", "executeUpdate");

	private static final Map<String, SQLInfoStructure> executedQueries = new ConcurrentHashMap<>();

	private static final Queue<PreparedStatementData> lastExecutedQueries = new ConcurrentLinkedQueue<>();

	private static final AtomicInteger queryCount = new AtomicInteger(0);

	private static final Map<String, Consumer<PreparedStatementData>> listeners = new ConcurrentHashMap<>();

	private static final Map<Long, PreparedStatementData> threadStatements = new ConcurrentHashMap<>();

	private static boolean isActive = false;

	/**
	 * This method gets called for every method executed on the jdbc objects
	 * like Connection and PreparedStatement.
	 * This thing works for multiple threads but assumes that the execution of those
	 * methods is linear:
	 * One method to create the preparedStatement
	 * Zero or more methods to set the parameters
	 * One method to execute the statement on the server
	 * 
	 * Should a PreparedStatement somehow be reused by the PersistenceService this
	 * thing will break. But at least in Hibernate this doesn's seem to be the case.
	 * I would have thought those would be cached somehow but whatever
	 */
	public void sqlTrace(SQLTraceRecord record) {
		if (!isActive) {
			return;
		}

		String methodName = record.getMethodName();
		// When start/stopping the listener we might end up in the middle of something
		// Instead of collecting a few NullPointerExceptions just do nothing
		// Some time later prepareStatement will add something into the statement map
		if (!methodName.equals("prepareStatement") && threadStatements.get(record.getThreadID()) == null) {
			return;
		}

		if (methodName.equals("prepareStatement")) {
			String sql = (String) record.getParams()[0];
			PreparedStatementData current = new PreparedStatementData(sql, record.getPoolName(), getFilteredStackTrace(),
					record.getTimeStamp());
			threadStatements.put(record.getThreadID(), current);
		} else if (methodName.equals("setNull")) {
			PreparedStatementData current = threadStatements.get(record.getThreadID());
			current.addParameter((Integer) record.getParams()[0], null);
		} else if (BINARY_STATEMENT_SETTERS.contains(methodName)) {
			PreparedStatementData current = threadStatements.get(record.getThreadID());
			current.addParameter((Integer) record.getParams()[0], BinaryData.VALUE);
		} else if (NON_BINARY_STATEMENT_SETTERS.contains(methodName)) {
			PreparedStatementData current = threadStatements.get(record.getThreadID());
			current.addParameter((Integer) record.getParams()[0], record.getParams()[1]);
		} else if (SEND_QUERY_TO_SERVER.contains(methodName)) {
			PreparedStatementData current = threadStatements.get(record.getThreadID());
			current.finish();
			lastExecutedQueries.add(current);
			if (lastExecutedQueries.size() > LAST_QUERIES_MAX_SIZE) {
				lastExecutedQueries.remove();
			}

			queryCount.incrementAndGet();

			SQLInfoStructure infos = executedQueries.computeIfAbsent(record.getPoolName(), key -> new SQLInfoStructure(key));
			infos.addQuery(current.getSqlSortable(), current.getStrackTrace());

			for (Consumer<PreparedStatementData> consumer : listeners.values()) {
				consumer.accept(current);
			}
		}
	}

	public static void toggle() {
		isActive = !isActive;
		// Listener was stopped, clear all currently tracked statements
		if (!isActive) {
			threadStatements.clear();
		}
	}

	public static boolean isActive() {
		return isActive;
	}

	public static void clear() {
		executedQueries.clear();
		lastExecutedQueries.clear();
		threadStatements.clear();
		queryCount.set(0);
	}

	public static Collection<SQLInfoStructure> getAll() {
		return executedQueries.values();
	}

	public static Queue<PreparedStatementData> getRecent() {
		return lastExecutedQueries;
	}

	public static int getCurrentQueryCount() {
		return queryCount.get();
	}

	public static void addListener(String id, Consumer<PreparedStatementData> consumer) {
		listeners.put(id, consumer);
	}

	public static void removeListener(String id) {
		listeners.remove(id);
	}

	/**
	 * Gets the current stacktrace, removes all stackframes from this package and
	 * some other noise like com.sun. In the end only stackframes from the calling
	 * project should be left
	 *
	 * @return The list of stacktraces, to to bottom.
	 *         The list is unmodifiable, because it will be a key in a map later on
	 */
	private List<String> getFilteredStackTrace() {
		List<String> result = new ArrayList<>();
		for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
			if (shouldAddStackTraceElement(stackTraceElement)) {
				result.add(stackTraceElement.toString());
			}
		}
		if (result.isEmpty()) {
			return List.of("Unknown Stacktrace (why? who knows)");
		} else {
			return Collections.unmodifiableList(result);
		}
	}

	private boolean shouldAddStackTraceElement(StackTraceElement stackTraceElement) {
		return PACKAGE_IGNORE_LIST.stream().noneMatch(entry -> {
			return stackTraceElement.getClassName().contains(entry);
		});
	}
}
