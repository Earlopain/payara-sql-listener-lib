package net.c5h8no4na.sqllistener;

import java.io.Serializable;
import java.util.List;

public class SingleSQLQuery implements Serializable {
	private static final long serialVersionUID = 1L;
	private String poolName;
	private String sqlSortable;
	private String sqlNoQuestionmarks;
	private List<String> stackTrace;
	private long timestamp;

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public String getSqlSortable() {
		return sqlSortable;
	}

	public void setSqlSortable(String sqlSortable) {
		this.sqlSortable = sqlSortable;
	}

	public String getSqlNoQuestionmarks() {
		return sqlNoQuestionmarks;
	}

	public void setSqlNoQuestionmarks(String sqlNoQuestionmarks) {
		this.sqlNoQuestionmarks = sqlNoQuestionmarks;
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
