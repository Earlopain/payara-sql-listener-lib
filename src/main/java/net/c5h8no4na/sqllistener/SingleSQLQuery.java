package net.c5h8no4na.sqllistener;

import java.io.Serializable;
import java.util.List;

public class SingleSQLQuery implements Serializable {
	private static final long serialVersionUID = 1L;
	private String poolName;
	private String sql;
	private List<String> stackTrace;
	private long timestamp;

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<String> getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(List<String> stackTrace) {
		this.stackTrace = stackTrace;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}