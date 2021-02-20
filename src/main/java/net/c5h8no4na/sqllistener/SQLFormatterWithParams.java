package net.c5h8no4na.sqllistener;

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
		int paramIndex = 1;
		while (true) {
			if (!params.containsKey(paramIndex)) {
				break;
			}
			Object nextParam = params.get(paramIndex);

			sqlWithQuestionsmarks = sqlWithQuestionsmarks.replaceFirst("\\?", getSQLParam(nextParam));
			paramIndex++;
		}

		return sqlWithQuestionsmarks;
	}

	public String getSQLParam(Object o) {
		if (o == null) {
			return "null";
		} else if (o instanceof String) {
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
