package net.c5h8no4na.sqllistener;

import java.util.HashMap;
import java.util.Map;

import com.github.vertical_blank.sqlformatter.SqlFormatter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;

/**
 * 
 * @author earlopain Takes a hibernate sql string and tries to make it a little
 *         more readable Removes the alias from columns and renames tables to
 *         the format of {tablename}_{occurancecount}
 */
public class SQLFormatter {

	private final String input;
	private final Map<String, Integer> tableNameCounter = new HashMap<>();
	private final Map<String, Table> tableMap = new HashMap<>();

	public SQLFormatter(String input) {
		this.input = input;
	}

	/**
	 * Beautifies hibernate sql queries
	 * 
	 * @return The formatted sql or the original if something goes wrong
	 */
	public String prettyPrint() {
		tableNameCounter.clear();
		tableMap.clear();
		Statement stmt;
		try {
			stmt = CCJSqlParserUtil.parse(input);
		} catch (JSQLParserException e) {
			return input;
		}

		if (stmt instanceof Select && ((Select) stmt).getSelectBody() instanceof PlainSelect) {
			Select select = (Select) stmt;
			formatSelect(select, (PlainSelect) select.getSelectBody());
		}

		return SqlFormatter.format(stmt.toString());
	}

	private void formatSelect(Select select, PlainSelect plainSelect) {
		select.accept(fixTableNames());

		for (SelectItem selectItem : plainSelect.getSelectItems()) {
			selectItem.accept(fixSelectItems());
		}

		if (plainSelect.getJoins() != null) {
			for (Join join : plainSelect.getJoins()) {
				join.getOnExpression().accept(fixExpressions());
			}
		}

		plainSelect.getWhere().accept(fixExpressions());
	}

	private TablesNamesFinder fixTableNames() {
		return new TablesNamesFinder() {
			@Override
			public void visit(Table table) {
				String originalTableAlias = table.getAlias().getName();
				table.setAlias(getTableAlias(table));
				tableMap.put(originalTableAlias, table);
			}
		};
	}

	private SelectItemVisitorAdapter fixSelectItems() {
		return new SelectItemVisitorAdapter() {
			@Override
			public void visit(SelectExpressionItem item) {
				item.getExpression().accept(fixExpressions());
				item.setAlias(null);
			}
		};
	}

	private ExpressionVisitorAdapter fixExpressions() {
		return new ExpressionVisitorAdapter() {
			@Override
			public void visit(Column column) {
				column.setTable(tableMap.get(column.getTable().getName()));
			}

			@Override
			public void visit(InExpression expression) {
				expression.accept(fixInExpression());
			}
		};
	}

	/**
	 * Removes all duplicate questionmarks from an InExpression
	 * 
	 * @return
	 */
	private ExpressionVisitorAdapter fixInExpression() {
		return new ExpressionVisitorAdapter() {
			@Override
			public void visit(ExpressionList list) {
				boolean containsQuestionmark = false;
				ExpressionList purgedList = new ExpressionList();
				for (Expression expression : list.getExpressions()) {
					if (expression.toString().equals("?")) {
						containsQuestionmark = true;
					} else {
						purgedList.addExpressions(expression);
					}
				}

				if (containsQuestionmark) {
					purgedList.addExpressions(new Column("?"));
				}
				list.setExpressions(purgedList.getExpressions());
			}
		};
	}

	private Alias getTableAlias(Table t) {
		Integer currentCount = tableNameCounter.get(t.getName());
		if (currentCount == null) {
			currentCount = 0;
		}
		Alias a = new Alias(t.getName() + "_" + currentCount);
		tableNameCounter.put(t.getName(), currentCount + 1);
		return a;
	}
}
