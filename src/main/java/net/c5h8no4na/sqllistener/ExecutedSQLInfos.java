package net.c5h8no4na.sqllistener;

public class ExecutedSQLInfos {
	private String stackTrace;
	private long timestamp;

	public ExecutedSQLInfos(String stackTrace, long timestamp) {
		this.stackTrace = stackTrace;
		this.timestamp = timestamp;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public long getTimestamp() {
		return timestamp;
	}
}