package net.c5h8no4na.sqllistener;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SQLQuery {

	private String sql;

	public SQLQuery(String sql) {
		this.sql = sql;
	}

	private int count = 0;
	private Queue<ExecutedSQLInfos> infos = new ConcurrentLinkedQueue<>();

	public void add(String stackTrace, long timestamp) {
		infos.add(new ExecutedSQLInfos(stackTrace, timestamp));
		count++;
	}

	public String getSql() {
		return sql;
	}

	public int getCount() {
		return count;
	}

	public Queue<ExecutedSQLInfos> getInfos() {
		return infos;
	}
}
