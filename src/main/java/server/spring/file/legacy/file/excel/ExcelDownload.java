package server.spring.file.legacy.file.excel;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.springframework.web.servlet.view.document.AbstractXlsxView;


/**
 * AbstractXlsxView : http contentType이 엑셀로 설정된다.
 * Excel File 생성후 파일을 내보낸다.
 * */
public class ExcelDownload extends AbstractXlsxView{

	private Workbook workbook;  // Excel 형식에 맞는 인스턴스 객체
	private Sheet sheet;        // Excel Sheet

	/** 1. 엑셀 파일 생성 */
	@Override
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		this.workbook = workbook;

		// 엑셀화 시킬 객체 리스트
		List<FileDetail> fileList =  (List<FileDetail>) modelMap.get("fileList");

		/** 1. 엑셀 파일 내부 sheet 생성 */
		sheet = workbook.createSheet("업로드파일내역");

		/** 2. 엑셀 헤더에 데이터 삽입  */
		renderCellHeader();

		/** 3. 엑셀 바디에 데이터 삽입  */
		renderCellBody(fileList);
	}

	/** 1-1. 엑셀 헤더에 데이터 삽입  */
	private void renderCellHeader() {
		Row headerRow = sheet.createRow(0);
		int columnIndex = 0;

		// FileDetail DTO 멤버변수에 접근
		for (Field field : new FileDetail().getClass().getDeclaredFields()) {

			if (field.isAnnotationPresent(ExcelHeaderColum.class)) {

				// 각 필드에 부여한 어노테이션의 정보를 가져온다.
				ExcelHeaderColum columnAnnotation = field.getAnnotation(ExcelHeaderColum.class);

				// 각 필드의 적용되어 있는 어노테이션의 값을 가져온다.
				/* @ExcelColumn(headerName = "아이디") */
		        String headerName = columnAnnotation.headerName();

		        // 행 내에 새 셀을 만들고 반환한다.
		        Cell headerCell = headerRow.createCell(columnIndex++);
		        // 셀의 값을 설정한다.
		        headerCell.setCellValue(headerName);
		        // 셀의 스타일을 설정한다
		        headerCell.setCellStyle(takeCellStyle("header"));
		        // 열의 넓이를 유동적으로 설정한다.
		        columnWidth(headerRow);
			}
		}
	}

	/** 1-2. 엑셀 바디에 데이터 삽입  */
	private void renderCellBody(List<FileDetail> FileList) throws Exception {

		for (int i = 0; i < FileList.size(); i++) {
			// 1. 시트 내에서 새 행을 만들고 열을 반환한다.
			Row bodyRow = sheet.createRow(sheet.getLastRowNum() + 1);
			// 반환할 값의 열 번호
			int columnIndex = 0;

			// 2. 해당 클래스에서 정의된 변수 리스트의 요소를 반복문을 통해서 추출한다.
			/* - Class 객체를 이용하여 해당 클래스의 생성자, 필드, 메소드 등 내부 정보를 조회 한다. ( REFLECTION ) */
			for(Field field : FileList.get(i).getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(ExcelHeaderColum.class)) {

					// 2-1. private 필드 ,메서드에 접근 하기 위한 설정
					field.setAccessible(true);

					// 2-2. get() 메소드에 해당 클래스의 인스턴스(new FileDetail())를 인자로 넘겨서 filed 값을 반환받는다.
					Object cellValue = field.get(FileList.get(i));

					// 2-3. 행 내에 새 셀을 만든다.
					Cell bodyCell = bodyRow.createCell(columnIndex++);

					// 2-4. filed의 데이터타입에 맞게 형변환을 해주고 셀의 값을 설정한다.
					/* cell.setCellValue 메서드는  타입이 어떻게 들어오냐에 따라 값의 표시형식을 정한다 */
					if (cellValue instanceof Number) {
						 Number numberValue = (Number) cellValue;
						 bodyCell.setCellValue(numberValue.doubleValue());
					} else if(cellValue instanceof String) {
						 bodyCell.setCellValue(cellValue.toString());
					}

					// 2-5. 셀의 스타일을 설정한다
					bodyCell.setCellStyle(takeCellStyle(""));
					// 2-6. 열의 넓이를 유동적으로 설정한다.
					columnWidth(bodyRow);
				}
			}
		}
	}

	/** 1-3. Cell Style 설정 */
	private CellStyle takeCellStyle(String cellType) {
		XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();

		// 중앙 정렬
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 세로
		cellStyle.setAlignment(HorizontalAlignment.CENTER);

		//본문 스타일 : 테두리
		cellStyle.setBorderBottom(BorderStyle.THICK);
		cellStyle.setBorderLeft(BorderStyle.THICK);
		cellStyle.setBorderRight(BorderStyle.THICK);
		cellStyle.setBorderTop(BorderStyle.THICK);

		if(cellType.equals("header")) {
			// Font 설정
			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 13);
			font.setBold(true);

			// 배경색 지정 (R,G,B 컬러 사용)
			cellStyle.setFillForegroundColor((short)41);
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); // 채우기 적용

		}
		return cellStyle;
	}

	/** 4. Column width 설정 */
	private void columnWidth(Row row) {
		// 반복문을 통해 각 열에 접근하여 컬럼 넓이를 설정한다.
		for(int colNum = 0; colNum<row.getLastCellNum(); colNum++) {
			// 컬럼 넓이 자동 조절
			workbook.getSheetAt(0).autoSizeColumn(colNum);
			workbook.getSheetAt(0).setColumnWidth(colNum, (workbook.getSheetAt(0).getColumnWidth(colNum))+(short)1024); //  자동조정한 사이즈에 (short)1024 추가
		}
	}
}
