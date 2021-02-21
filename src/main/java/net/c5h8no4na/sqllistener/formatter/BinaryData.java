package net.c5h8no4na.sqllistener.formatter;

public class BinaryData {
	public static BinaryData VALUE = new BinaryData();

	private BinaryData() {}

	@Override
	public String toString() {
		return "<binary data>";
	}
}
