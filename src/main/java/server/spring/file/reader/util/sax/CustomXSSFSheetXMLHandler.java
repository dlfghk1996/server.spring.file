//package server.spring.file.reader.util.sax;
//
//import org.apache.poi.ss.usermodel.DataFormatter;
//import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
//import org.apache.poi.xssf.model.SharedStrings;
//import org.apache.poi.xssf.model.Styles;
//
//public class CustomXSSFSheetXMLHandler extends XSSFSheetXMLHandler {
//    private SaxCustomHandler saxCustomHandler;
//
//    public CustomXSSFSheetXMLHandler(Styles styles,SharedStrings strings, SheetContentsHandler sheetContentsHandler, boolean formulasNotResults) {
//        super(styles, strings, sheetContentsHandler, new DataFormatter(), formulasNotResults);
//        this.saxCustomHandler = (SaxCustomHandler) sheetContentsHandler;
//    }
//
//
//    /**
//     * org.xml.sax.helpers.DefaultHandler 문서 끝 알림을 받습니다.
//     * *기본적으로는 아무 작업도 수행하지 않습니다.
//     * 애플리케이션 작성자는 문서 끝에서 특정 작업(예: 트리 마무리 또는 출력 파일 닫기)을 수행하기 위해 서브클래스에서 이 메서드를 재정의할 수 있습니다.
//     */
//
//    @Override
//    public void endDocument() {
//        System.out.println("문서 끝");
//        this.saxCustomHandler.callNext();
//    }
//}
