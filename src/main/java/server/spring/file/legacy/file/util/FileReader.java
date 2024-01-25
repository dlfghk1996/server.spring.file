package server.spring.file.legacy.file.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import server.spring.file.legacy.file.excel.SheetHandler;
import server.spring.file.legacy.file.excel.SheetHelper;

/** 업로드된 파일을 읽기 위한, 파일 타입별(xls,csv, txt) 파일 읽기 기능 구현 class */
@Component
public class FileReader {

   public static String sheetnames = null;

   // xlxs 데이터 반환 객체
   public static List<List<String>> rows = new ArrayList<List<String>>(); // sheet<행<열>>
   public static List<SheetHelper> sheet_list = new ArrayList<SheetHelper>();   // map<<sheetname, sheet<행<열>>>

   /** 1. xls, xlsx 읽기*/
   // 단일 시트 또는 파일의 모든 시트를 가져 오기

   public SheetHandler readExcel(File file) throws Exception {
		SheetHandler sheetHandler= new SheetHandler();

		// 1. OPCPackage : 파일을 읽거나 쓸수 있는 상태의 컨테이너 생성
		OPCPackage opcPackage = OPCPackage.open(file);

		// 2. XSSFReader : OPC 컨테이너를 XSSF 형식으로 읽어옴
		XSSFReader xssfReader = new XSSFReader(opcPackage); // 문자열 테이블과 스타일에 대한 인터페이스를 선택적으로 제공

		// 3. 파일의 데이터를 Table형식으로 읽을 수 있도록 지원
		ReadOnlySharedStringsTable sharedStringsTable = new ReadOnlySharedStringsTable(opcPackage);

		// 4. 읽어온 Table에 적용되어 있는 Style
		StylesTable stylesTable = xssfReader.getStylesTable();

		// 5. SAX parser 방식의  XMLReader : XML 데이터를 이동하고 노드의 내용을 읽는다

        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        XMLReader xmlReader = parserFactory.newSAXParser().getXMLReader();

		/* 6. 구문분석하는데 필요한 개체를 인자로 넘겨서 엑셀시트를 분석한다.
			XSSFSheetXMLHandler : 엑셀 Sheet 또는 XML의 처리를 위해 행 및 셀 이벤트를 생성*/
		ContentHandler handler = new XSSFSheetXMLHandler(
			stylesTable,
			sharedStringsTable,
			sheetHandler,
			false);

		// 7. 현재 ContentHandler를 설정. ContentHandler가 설정되지 않으면, 내용 이벤트가 버려진다.
		xmlReader.setContentHandler(handler);

		// 복수 Sheets일 경우 : sheet별 collection으로 분할해서 가져옴
		// Excel sheet가 3장 이상일 경우 더이상 sheet를 읽지않는다. (최대 3장으로 제한)
		SheetIterator itr = (SheetIterator)xssfReader.getSheetsData();

		int i = 0;
		while(itr.hasNext() && i < 4) {

			i++;

			InputStream sheet = itr.next();
			sheetnames = itr.getSheetName();
			// 8. 엔티티를 읽기 위해 XMLReader에 필요한 정보를 캡슐화 한다. -> 읽을 InputSource 반환
		    InputSource sheetSource = new InputSource(sheet);

		    // 9. source로 입력된 XML문서를 파싱하면서 SAX이벤트를 발생 => SheetHadler로 sheet를 읽는다.
		    xmlReader.parse(sheetSource);

		    sheet_list.add(new SheetHelper(i,sheetnames,rows)); //TEST 코드

		    // 각 시트를 읽고 list를 초기화 한다.(ex. 바구니 비우기)
		    rows = new ArrayList<List<String>>();

		    sheet.close();
		}

       // 10. xml 문서 안의 정보들이 파싱되면서 순서대로 이벤트가 ContentHandler을 통해서 호출된다
	   // 엑셀파일을 다 읽으면 return 된다.
       return sheetHandler;
   }

   /** 2. CSV 읽기  */
   public List<String[]> readCsv (File file) throws Exception {

	   List<String[]> csvList = new ArrayList<String[]>(); // 전체 행을 담는 list

	   /* CSV 는 다양한 방식으로 나타내기 때문에,구문 분석 할수 있는 CSV 형식을 정한다.
	   	    - 정해진 형식 : DEFAULT, EXCEL, RFC4180 등
	   	    - @withQuote : 따옴표 문자를 설정하거나 비활성화 설정
	   */
	   try {
		   // 1. Excel 파일 형식 & '"' 인용문자 설정
		   CSVFormat csvFormat = CSVFormat.EXCEL.withQuote('"');
		   csvList = csvParser(csvFormat, file);
	   } catch (IOException e) {
		   try {
			   // 2. Excel 파일 형식 & 인용문자 비활성화
			   CSVFormat csvFormat_Quote_null = CSVFormat.EXCEL.withQuote(null);
			   csvList = csvParser(csvFormat_Quote_null, file);

		   } catch (IOException e1) {
			   // 3. 탭으로 구분되는 형식
			   CSVFormat csvFormat_default = CSVFormat.TDF;
			   csvList = csvParser(csvFormat_default, file);
		   }
	   } catch (Exception e1) {
		   throw new IOException("파일을 읽을수 없습니다. 파일 형식을 다시 확인 해주세요.");
	   }

	return csvList;

   }

   /** CSV 형식 지정 (EXCEL, TDF)  */
   public List<String[]> csvParser(CSVFormat csvFormat, File file) throws Exception {

	   // 1. 각 파일의 인코딩을 체크한다
	   String charset = encoding(file);

	   // 2. 위에서 추출한 인코딩을 스트림의 인코딩으로 지정하여 바이트단위로 파일을 읽어 들인다.
	   BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));

	   // 3. 지정된 형식에 따라 CSV 파일을 구문 분석합니다
	   // ** 레코드가 입력 스트림에서 구문 분석되면 되돌릴 수 없다.
	   CSVParser parser = new CSVParser(fileReader, csvFormat);
	   List<CSVRecord> records = parser.getRecords();

	   // 3. return Object : 전체 행을 담는 list
	   List<String[]> csvList = new ArrayList<String[]>();

	   // 4. CSV 파일에서 구문 분석된 CSV 레코드를 반복문을 돌려 출력한다.
	   for (CSVRecord record : records) {
		   int i=0;
		   // 배열 선언과 동시에 각 행 의 열 사이즈  크기 할당
		   String [] row = new String[record.size()];
		   // 행 의 열 길이만큼 반복문 실행
		   for (String field : record) {
			   row[i++] = field;
			   // 최대 출력가능한 열 길이 제한(15)
			  // if(i < 16) {
				  // row[i++] = field;
			  // }
		   }
		   csvList.add(row);
	   }

	   // 5. parser 닫기
	   parser.close();

	   return csvList;
   }

   /** CSV 파일 인코딩 감지 */
   public String encoding(File file) throws Exception {

	   byte[] buf = new byte[4096];
	   FileInputStream fis = new FileInputStream(file);

	   // 1.객체 생성
	   UniversalDetector detector = new UniversalDetector(null);

	   // 2.UniversalDetector.handleData()를 호출하여 일부 데이터(일반적으로 수천 바이트)를 감지기에 공급한다
	   int nread;
	   while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
	     detector.handleData(buf, 0, nread);
	   }

	   // 3.UniversalDetector.dataEnd()를 호출하여 데이터의 끝을 감지기에 알린다.
	   detector.dataEnd();

	   // 4.UniversalDetector.getDetectedCharset()을 호출하여 감지된 인코딩 이름을 가져온다.
	   String encoding = detector.getDetectedCharset();
	   encoding = encoding == null?"euc-kr":encoding;

	   //5.감지기 인스턴스를 재사용하기 전에 UniversalDetector.reset()을 호출해야한다.
	   detector.reset();
	   return encoding;
   }


   /** 3. TXT 읽기 */
   public List<String> readTxt(File file) throws Exception {
      List<String> txtList = new ArrayList<String>();
      BufferedReader bufferedReader = new BufferedReader(new java.io.FileReader(file));
      String line = null;
      while((line = bufferedReader.readLine()) != null ) {
            txtList.add(line);
         }
      bufferedReader.close();
      return txtList;
   }
}