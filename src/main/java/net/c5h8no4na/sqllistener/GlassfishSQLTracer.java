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

	private static final List<String> BINARY_STATEMENT_SETTERS = Arrays.asList("setAsciiStream", "setBinaryStream", "setBlob", "setBytes",
			"setCharacterStream", "setClob", "setNCharacterStream", "setNClob");

	private static final List<String> NON_BINARY_STATEMENT_SETTERS = Arrays.asList("setBigDecimal", "setBoolean", "setByte", "setDate",
			"setDouble", "setFloat", "setInt", "setLong", "setNString", "setString", "setShort", "setTime", "setTimestamp");

	private static final List<String> SEND_QUERY_TO_SERVER = Arrays.asList("execute", "executeQuery", "executeUpdate");
	private static final Map<String, SQLInfoStructure> executedQueries = new ConcurrentHashMap<>();

	private static final Map<String, Consumer<SingleSQLQuery>> listeners = new ConcurrentHashMap<>();

	private static final Map<Long, CurrentPreparedStatement> threadStatements = new ConcurrentHashMap<>();

	private static boolean isActive = true;

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
		if (methodName.equals("prepareStatement")) {
			String sql = (String) record.getParams()[0];
			CurrentPreparedStatement current = new CurrentPreparedStatement(sql);
			threadStatements.put(record.getThreadID(), current);
		} else if (methodName.equals("setNull")) {
			CurrentPreparedStatement current = threadStatements.get(record.getThreadID());
			current.addParameter((Integer) record.getParams()[0], null);
		} else if (BINARY_STATEMENT_SETTERS.contains(methodName)) {
			CurrentPreparedStatement current = threadStatements.get(record.getThreadID());
			current.addParameter((Integer) record.getParams()[0], "<binary data>");
		} else if (NON_BINARY_STATEMENT_SETTERS.contains(methodName)) {
			CurrentPreparedStatement current = threadStatements.get(record.getThreadID());
			current.addParameter((Integer) record.getParams()[0], record.getParams()[1]);
		} else if (SEND_QUERY_TO_SERVER.contains(methodName)) {
			CurrentPreparedStatement current = threadStatements.get(record.getThreadID());

			List<String> stackTrace = getFilteredStackTrace();
			String sqlSortable = current.getSQLSortable();
			String sqlNoQuestionmarks = current.getSQLReplaceQuestionmarks();

			SQLInfoStructure infos = executedQueries.computeIfAbsent(record.getPoolName(), key -> new SQLInfoStructure(key));
			infos.addQuery(current.getSQLSortable(), stackTrace, record.getTimeStamp());

			SingleSQLQuery query = new SingleSQLQuery();
			query.setPoolName(record.getPoolName());
			query.setSqlSortable(sqlSortable);
			query.setSqlNoQuestionmarks(sqlNoQuestionmarks);
			query.setStackTrace(stackTrace);
			query.setTimestamp(record.getTimeStamp());

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
					q.setSqlSortable(sql);
					q.setSqlNoQuestionmarks(sql);
					q.setStackTrace(c.getStackTrace());
					q.setTimestamp(c.getTimestamp());
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
