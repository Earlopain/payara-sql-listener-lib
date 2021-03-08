package net.c5h8no4na.sqllistener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQLQuery {

	private String sql;

	private Map<List<String>, Integer> stackTraces = new ConcurrentHashMap<>();

	private int totalCount = 0;

	public SQLQuery(String sql) {
		this.sql = sql;
	}

	public void add(List<String> stackTrace) {
		Integer stackTraceCount = stackTraces.computeIfAbsent(stackTrace, s -> Integer.valueOf(0));
		stackTraces.put(stackTrace, stackTraceCount + 1);
		totalCount++;
	}

	public String getSql() {
		return sql;
	}

	public Map<List<String>, Integer> getStackTraces() {
		return stackTraces;
	}

	public int getTotalCount() {
		return totalCount;
	}
}
