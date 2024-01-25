package server.spring.file.reader.util.sax;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.binary.XSSFBSheetHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.helpers.DefaultHandler;
import server.spring.file.reader.util.ExcelMetadata;
import server.spring.file.reader.util.ExcelEventHandlerByRows;

@Slf4j
public class SaxCustomHandler<T> extends DefaultHandler implements SheetContentsHandler {
    private final int TRANSACTION = 100;
    private List<String> rows = new ArrayList<>();
    private List<String> headers = new ArrayList<>();
    private int curColNum = -1;
    private int curRowNum = -1;
    private T obj;
    private List<T> resultList = new ArrayList<>();
    private ExcelMetadata excelMetadata;
    private ExcelEventHandlerByRows excelEventHandlerByRows; // doNext(  target.forEach(t -> this.dataList.add((ExcelSampleDto) t));)
    private ExcelSaxReader excelSaxReader;

    public SaxCustomHandler(ExcelMetadata excelMetadata, ExcelEventHandlerByRows excelEventHandlerByRows, ExcelSaxReader excelSaxReader) {
        this.excelMetadata = excelMetadata;
        this.excelEventHandlerByRows = excelEventHandlerByRows;
        this.obj = (T) this.excelMetadata.getInstance(); // ExcelSampleDto(columnA=null, columnB=null)
        this.excelSaxReader = excelSaxReader;
    }

    @Override
    public void hyperlinkCell(String cellReference, String formattedValue, String url, String toolTip, XSSFComment comment) {
    }

    // 행 시작
    @Override
    public void startRow(int rowNum) {
        System.out.println("행시작: " + rowNum);
        this.curColNum = -1;
        this.curRowNum += 1;
    }

    // 행 끝
    @Override
    public void endRow(int rowNum) {
        System.out.println("행끝: " + rowNum);
        System.out.println(this.rows.toString());
        // 첫번째 행 ()
        if (rowNum == 0) {
            // List<List<String>> rowsData
            this.headers = new ArrayList<>(this.rows);
        } else {
            // 행보다 열 길이가 작을경우 빈값으로 맞춰준다.
            if (this.rows.size() < this.headers.size()) {
                for (int i = this.rows.size(); i < this.headers.size(); i++) {
                    this.rows.add("");
                }
            }
            System.out.println(this.obj);
            this.resultList.add(this.obj);
            this.obj = (T) this.excelMetadata.getInstance();
            System.out.println(this.obj);
        }
        if (this.curRowNum < this.TRANSACTION) {
        //if (this.curRowNum >= this.TRANSACTION) {
            callNext();
            this.curRowNum = 0;
            this.resultList.clear();
        }

        System.out.println(this.rows.toString());
        this.rows.clear();
    }

    /** 각 Row 의 각 셀을 읽을 때 */
    @Override
    public void cell(String columnName, String value, XSSFComment comment) {
        System.out.println("cell");

        // icol = cellName to Integer
        int icol = (new CellReference(columnName)).getCol();
        int emptyCol = icol - this.curColNum - 1;

        System.out.println("icol: " + icol);
        System.out.println("emptyCol: " +emptyCol);
        // 읽은 Cell의 번호를 이용하여 빈Cell 자리에 빈값을 강제로 저장시켜줌
        for (int i = 0; i < emptyCol; i++) {
            this.rows.add("");
            this.curColNum = emptyCol;
            setValue("" , i);
        }

        // 현재 column 번호로 할당
        this.rows.add(value);
        // 행 2번째 부터 (=header 행 다음행)
        if (this.curRowNum > 0) setValue(value, icol);
        this.curColNum = icol;
    }

    public void callNext() {
        Map<T, String> errMap = new HashMap<>();
        this.excelEventHandlerByRows.doNext(this.resultList, errMap);
        excelSaxReader.setDataList(this.resultList);
    }

    private void setValue(String value, int colNum) {
        System.out.println("this.curColNum: " + this.curColNum + " setValue: " + value);
        Optional<Field> optionalField = this.excelMetadata.getFieldByHeader(this.headers.get(colNum));
        optionalField.ifPresent(field-> {
            field.setAccessible(true);
            try {
                setValueFromString(field, value);
            } catch (IllegalAccessException ex) {
                System.out.println("IllegalAccessException");
                ex.printStackTrace();
            }
        });
    }

    private void setValueFromString(Field field, String contentBuffer) throws IllegalAccessException {
        // dto 의 필드에 맞춰 데이터 타입 추가
        if (field.getType().equals(int.class)) {
            field.set(this.obj, Integer.parseInt(contentBuffer));
        } else {
            field.set(this.obj, contentBuffer);
        }
    }

    /**
     * org.xml.sax.helpers.DefaultHandler 문서 끝 알림을 받습니다.
     * *기본적으로는 아무 작업도 수행하지 않습니다.
     * 애플리케이션 작성자는 문서 끝에서 특정 작업(예: 트리 마무리 또는 출력 파일 닫기)을 수행하기 위해 서브클래스에서 이 메서드를 재정의할 수 있습니다.
     */

    @Override
    public void endDocument() {
        System.out.println("문서 끝");
        this.callNext();
    }
}
