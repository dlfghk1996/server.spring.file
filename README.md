# server.spring.file
확장자별 파일을 읽기(csv, xlxs, txt 등) &amp; 파일 다운로드 &amp; AWS S3 manager


##### 개발 환경 
- Java 17 </br>
- Spring boot  3.2.2

* * *

##### 주요 라이브러리
- openCSV
- Apache POI
- Apache Commons CSV
- ItextPdf
- Apache PDFBox
* * *

##### 기능
1. File Reader
   - <span style="color:green"> [xlxs]</span> <span style="color:grey"> "XSSFReader & SAX" 를 이용한 xlxs 파일 대용량 업로드 
   - <span style="color:green"> [xlxs]</span> <span style="color:grey"> "XSSFWorkbook" 을 이용한 xlxs 파일 업로드
   - <span style="color:green"> [csv]</span> <span style="color:grey"> "opencsv" : CSVReaderBuilder 이용한 Bean 기반 Reading
   - <span style="color:green"> [csv]</span> <span style="color:grey"> "commons csv" : CSVParser 이용하여 지정된 형식에 따라 CSV 파일 구문 분석
2. File Viewer
   - 파일 조건 : csv, txt, xlsx, img 파일(summernote 사용)
   - 파일 용량 : 1MB 이하
3. File Download 및 파일 확장자 변환
   - DB 데이터 csv 다운로드
   - img <-> pdf 변환
   - img 파일 1개 이상 시 zip 압축
4. s3 파일 관리 (upload, download, delete 등)
