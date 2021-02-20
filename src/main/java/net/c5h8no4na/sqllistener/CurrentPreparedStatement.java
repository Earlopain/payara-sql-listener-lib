package net.c5h8no4na.sqllistener;

import java.util.HashMap;
import java.util.Map;

public class CurrentPreparedStatement {
	private String rawSQL;
	private Map<Integer, Object> parameter = new HashMap<>();

	public CurrentPreparedStatement(String rawSQL) {
		this.rawSQL = rawSQL;
	}

	public void addParameter(int position, Object param) {
		parameter.put(position, param);
	}

	public String getSQLReplaceQuestionmarks() {
		SQLFormatter formatter = new SQLFormatterWithParams(rawSQL, parameter);
		return formatter.prettyPrint();
	}

	public String getSQLSortable() {
		SQLFormatter formatter = new SQLFormatter(rawSQL);
		return formatter.prettyPrint();
	}

}
