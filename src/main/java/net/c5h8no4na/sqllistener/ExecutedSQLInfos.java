package net.c5h8no4na.sqllistener;

import java.util.List;

public class ExecutedSQLInfos {
	private List<String> stackTrace;
	private long timestamp;

	public ExecutedSQLInfos(List<String> stackTrace, long timestamp) {
		this.stackTrace = stackTrace;
		this.timestamp = timestamp;
	}

	public List<String> getStackTrace() {
		return stackTrace;
	}

	public long getTimestamp() {
		return timestamp;
	}
}