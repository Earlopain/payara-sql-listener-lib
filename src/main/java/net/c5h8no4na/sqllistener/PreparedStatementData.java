package net.c5h8no4na.sqllistener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreparedStatementData {
	private final transient String rawSQL;
	private final transient Map<Integer, Object> parameter = new HashMap<>();
	private final String poolName;
	private final List<String> stackTrace;
	private final long timestamp;
	private String sqlNoQuestionmarks;
	private String sqlSortable;

	public PreparedStatementData(String rawSQL, String poolName, List<String> stackTrace, long timestamp) {
		this.rawSQL = rawSQL;
		this.poolName = poolName;
		this.stackTrace = stackTrace;
		this.timestamp = timestamp;
	}

	public void addParameter(int position, Object param) {
		parameter.put(position, param);
	}

	public void finish() {
		SQLFormatter formatter1 = new SQLFormatter(rawSQL);
		SQLFormatter formatter2 = new SQLFormatterWithParams(rawSQL, parameter);
		sqlSortable = formatter1.prettyPrint();
		sqlNoQuestionmarks = formatter2.prettyPrint();
	}

	public String getSqlNoQuestionmarks() {
		return sqlNoQuestionmarks;
	}

	public String getSqlSortable() {
		return sqlSortable;
	}

	public String getPoolName() {
		return poolName;
	}

	public List<String> getStrackTrace() {
		return stackTrace;
	}

	public long getTimestamp() {
		return timestamp;
	}

}
