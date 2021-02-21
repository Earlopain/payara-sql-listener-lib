package net.c5h8no4na.sqllistener;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;

public class SQLFormatterWithParams extends SQLFormatter {

	protected Map<Integer, Object> params;

	public SQLFormatterWithParams(String input, Map<Integer, Object> params) {
		super(input);
		this.params = params;
	}

	@Override
	public String prettyPrintNoFormatting() {
		String sqlWithQuestionsmarks = super.prettyPrintNoFormatting();

		if (params.size() == 0) {
			return sqlWithQuestionsmarks;
		}

		List<Integer> questionmarkIndex = new ArrayList<>();
		for (int i = 0; i < sqlWithQuestionsmarks.length(); i++) {
			if (sqlWithQuestionsmarks.charAt(i) == '?') {
				questionmarkIndex.add(i);
			}
		}

		if (questionmarkIndex.size() != params.size()) {
			return input;
		} else {
			StringBuilder result = new StringBuilder();
			int start = 0;
			int end = 0;
			for (int i = 0; i < questionmarkIndex.size(); i++) {
				end = questionmarkIndex.get(i);
				result.append(sqlWithQuestionsmarks.substring(start, end));
				result.append(getSQLParam(params.get(i + 1)));
				start = end + 1;
			}
			result.append(sqlWithQuestionsmarks.substring(end + 1));
			return result.toString();
		}
	}

	public String getSQLParam(Object o) {
		if (o == null) {
			return "null";
		} else if (o instanceof String) {
			return String.format("\"%s\"", o);
		} else if (o instanceof Date || o instanceof Time || o instanceof Timestamp) {
			return String.format("\"%s\"", o);
		} else {
			return o.toString();
		}
	}

	/**
	 * Don't fix InExpressions when replacing ?, because that would change the
	 * number of parameters
	 */
	@Override
	protected ExpressionVisitorAdapter fixInExpression() {
		return new ExpressionVisitorAdapter() {};
	}

}
