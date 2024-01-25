package server.spring.file.reader.util.sax;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import server.spring.file.reader.dto.ExcelSampleDto;
import server.spring.file.reader.util.ExcelMetadata;
import server.spring.file.reader.util.ExcelEventHandlerByRows;

@Slf4j
public class ExcelSaxReader<T> {
    private ExcelMetadata excelMetadata;
    private ExcelEventHandlerByRows excelEventHandlerByRows;
    private List<T> dataList = new ArrayList<>();

    public <T> ExcelSaxReader(Class<T> type, ExcelEventHandlerByRows excelEventHandlerByRows) {
        this.excelMetadata = new ExcelMetadata<>(type);
        this.excelEventHandlerByRows = excelEventHandlerByRows;
    }

    public <T> void readExcelBySerial(MultipartFile file) {
        try {
            // xlsx -> OOXML
            OPCPackage opc = OPCPackage.open(file.getInputStream());
            XSSFReader reader = new XSSFReader(opc);

            // 데이터를 Table형식으로 읽을 수 있도록 지원
            ReadOnlySharedStringsTable sharedStringsTable = new ReadOnlySharedStringsTable(opc);

            // SAX parser 방식의  XMLReader : XML 데이터를 이동하고 노드의 내용을 읽는다
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            XMLReader xmlReader = parserFactory.newSAXParser().getXMLReader();

            /* 6. 구문분석하는데 필요한 개체를 인자로 넘겨서 엑셀시트를 분석한다.
			XSSFSheetXMLHandler : 엑셀 Sheet 또는 XML의 처리를 위해 행 및 셀 이벤트를 생성*/
            ContentHandler handler = new XSSFSheetXMLHandler(
                    reader.getStylesTable(),
                    sharedStringsTable,
                    new SaxCustomHandler<>(this.excelMetadata, this.excelEventHandlerByRows, this),
                    false
            );

            // 현재 ContentHandler를 설정. ContentHandler가 설정되지 않으면, 내용 이벤트가 버려진다.
            xmlReader.setContentHandler(handler);

            // source로 입력된 XML문서를 파싱하면서 SAX이벤트를 발생 => SheetHadler로 sheet를 읽는다.
            // 첫 번째 sheet 만 읽음(다중 처리 시 while 문 사용)
            InputStream inputStream = reader.getSheetsData().next();
            InputSource inputSource = new InputSource(inputStream);

            xmlReader.parse(inputSource);

            inputStream.close();
            opc.close();
        } catch (OpenXML4JException | SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDataList(List<T> rowList){
        log.info("target: {}", rowList.toString());
        rowList.forEach(row -> this.dataList.add((T) row));
    }

    public List<T> getDataList(){
        return this.dataList;
    }
}
