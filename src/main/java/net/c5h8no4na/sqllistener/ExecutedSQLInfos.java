package net.c5h8no4na.sqllistener;

import java.util.List;

public class ExecutedSQLInfos {
	private List<String> stackTrace;
	private long timestamp;
	private long executionTime;

	public ExecutedSQLInfos(List<String> stackTrace, long timestamp, long executionTime) {
		this.stackTrace = stackTrace;
		this.timestamp = timestamp;
		this.executionTime = executionTime;
	}

	public List<String> getStackTrace() {
		return stackTrace;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getExecutionTime() {
		return executionTime;
	}
}