package server.spring.file.legacy.file.excel;

import java.util.List;

/**
 * @author ilhwa
 *
 */
public class SheetHelper {
	private int sheet_num;
	private String sheet_name;
	private List<List<String>> sheet;

	public SheetHelper() {
	}

	public SheetHelper(int sheet_num, String sheet_name, List<List<String>> sheet) {
		super();
		this.sheet_num = sheet_num;
		this.sheet_name = sheet_name;
		this.sheet = sheet;
	}

	public int getSheet_num() {
		return sheet_num;
	}
	public void setSheet_num(int sheet_num) {
		this.sheet_num = sheet_num;
	}
	public String getSheet_name() {
		return sheet_name;
	}
	public void setSheet_name(String sheet_name) {
		this.sheet_name = sheet_name;
	}
	public List<List<String>> getSheet() {
		return sheet;
	}
	public void setSheet(List<List<String>> sheet) {
		this.sheet = sheet;
	}

	@Override
	public String toString() {
		return "SheetHelper [sheet_num=" + sheet_num + ", sheet_name=" + sheet_name + ", sheet=" + sheet + "]";
	}

}
