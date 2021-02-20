package net.c5h8no4na.sqllistener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQLInfoStructure {
	private String poolName;

	private Map<String, SQLQuery> queries = new ConcurrentHashMap<>();

	public SQLInfoStructure(String poolName) {
		this.poolName = poolName;
	}

	public void addQuery(String sql, List<String> stackTrace, long timestamp) {
		SQLQuery infos = queries.computeIfAbsent(sql, key -> new SQLQuery(key));
		infos.add(stackTrace, timestamp);
	}

	public String getPoolName() {
		return poolName;
	}

	public Map<String, SQLQuery> getQueries() {
		return queries;
	}

}
