package net.c5h8no4na.sqllistener;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SQLQuery {

	private String sql;

	public SQLQuery(String sql) {
		this.sql = sql;
	}

	private Queue<ExecutedSQLInfos> infos = new ConcurrentLinkedQueue<>();

	public void add(List<String> stackTrace, long timestamp) {
		infos.add(new ExecutedSQLInfos(stackTrace, timestamp));
	}

	public String getSql() {
		return sql;
	}

	public Queue<ExecutedSQLInfos> getInfos() {
		return infos;
	}
}
