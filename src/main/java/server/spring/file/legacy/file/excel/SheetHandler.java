package server.spring.file.legacy.file.excel;

import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import server.spring.file.legacy.file.util.FileReader;

/** 엑셀에 입력되어 있는 nXn 정보를 2차원 ArrayList에 그대로 변환하는 기능 */
public class SheetHandler implements SheetContentsHandler{

	private List<String> row; // 각 행의 데이터가 들어가는 반환 객체

	//빈 값을 체크하기 위해 사용할 셀번호
	private int currentCol = -1;
	private int currentRow = 0;

	// 행 시작
	@Override
	public void startRow(int rowNum) {
		this.currentRow = rowNum; // 열
		this.currentCol = -1;
		// 행이 시작할때 마다 한 행을 담을 리스트 객체를 새롭게 생성한다.
		row = new ArrayList<String>();
	}

	// 행 끝
	@Override
	public void endRow(int rowNum) {
		FileReader.rows.add(row); // 반환객체에 추가
	}

	/** 각 Row 의 각 셀을 읽을 때 */
	@Override
	public void cell(String columnName, String value, XSSFComment comment) {

		// cellReference : cell name
		// formattedValue : cell value
		// icol = cellName to Integer

		int iCol = (new CellReference(columnName)).getCol();
		int emptyCol = iCol - currentCol - 1;

		//읽은 Cell의 번호를 이용하여 빈Cell 자리에 빈값을 강제로 저장시켜줌
		for(int i = 0 ; i < emptyCol ; i++) {
			row.add("");
		}

		// 현재 column 번호로 할당
		currentCol = iCol;

		// 출력할수 있는 열의 길이 제한 (15)
		 //if(row.size() < 15) {
		//	 System.out.println("열길이 제한");
		//	 row.add(value);
		// }
		// 각 column에 데이터를 담는다.
		row.add(value);
	}

	/** 해당 엑셀의 머리말 꼬리말에 적혀 있는 값 */
	@Override
	public void headerFooter(String text, boolean isHeader, String tagName) {
	}
}
