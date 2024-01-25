package server.spring.file.legacy.file.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.coyote.BadRequestException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.AbstractView;
import server.spring.file.legacy.file.csv.CustomMappingStrategy;
import server.spring.file.legacy.file.excel.FileDetail;

/** 파일 업로드 & 삭제 & 생성 관련 기능 */
@Component
public class FileManager extends AbstractView{

	@Autowired
	private ServletContext servletContext;


	/** 1. 파일  업로드 */
	public List<FileDetail> uploadFile(List<MultipartFile> multipartFiles,String uploadType) throws Exception {
		List<FileDetail> fileDetailList = new ArrayList<FileDetail>();

		String filename = "";
		// 업로드 파일 저장 경로 설정
		String path = setFilePath(uploadType);

		for(MultipartFile multipartFile : multipartFiles) {
			 String originalFilename = multipartFile.getOriginalFilename();
			 // 업로드된 파일명의 중복을 방지하기 위해 파일명을 식별자로 만든다.
			 filename = UUID.randomUUID().toString();
			 // 파일 객체 생성
			 File file = new File(path, filename);

			 // file 디렉토리가 없을경우 디렉토리 생성
			 if(!file.exists()) {
				 file.mkdirs();
			 };

			 // 업로드한 파일을 지정한 경로에 저장
			 multipartFile.transferTo(file);

			 // 업로드 한 파일 정보를 db에 저장하기 위해 FileDetail객체 생성 한 뒤  리스트에 추가(예외.summernote upload는 글 안에 이미지 태그 자체로 저장된다.)
		     fileDetailList.add(setFileDetail(originalFilename, filename, uploadType, multipartFile.getSize(), path));

		 }
		 return fileDetailList;
	 }


	 /** 2. 파일 경로 생성 */
	 protected String setFilePath(String uploadType) {
		 // 1. 프로젝트 내부 resource 폴더에 저장 하기 위해, 현재 구동중인 서블릿의 경로를 구한다.
		 String real_path = servletContext.getRealPath("/");
		 // 2. 상세 경로
		 String resoure_path = "resources/img/upload/board/";
		 // 3. 현재 날짜로 폴더 생성
		 String currentTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		 // 4. 업로드 기능 종류를 구분자로 하여, 각 기능별 업로드 경로 지정
		 String path = "";
		 switch (uploadType) {
			case "upload": path = real_path + resoure_path + "fileBoard/" + currentTime; break;         // 파일 게시판 업로드 경로
			case "convert": path = real_path + resoure_path + "fileBoard/convert"; break;               // 확장자 변환을 위한 임시 업로드 경로
			case "summernote": path = real_path + resoure_path + "summernote/" + currentTime; break;    // 게시판 썸머노트에디터 이미지 업로드 경로
			default: break;
		 }

		 return path;
	 }

	 /** 3. FileDetail 객체생성 */
	 protected FileDetail setFileDetail(String originalFilename, String filename, String uploadType, long fileSize, String path) {

		 // summernote 업로드 일 경우 파일을 불러와야 되므로, sertvlet.xml 에서 설정한 경로로 수정한다.
		 if(uploadType.equals("summernote")) {
			 path = path.substring(path.indexOf("summernote"));
		 }

		 // 파일 확장자 추출
		 String filetype = StringUtils.getFilenameExtension(originalFilename);

		 // 미리보기 구분자
		 String readAble = "n";

		 // 미리보기가 가능한 파일 확장자
		 String[] filetypeList = {"csv", "txt", "xls", "xlsx", "jpg", "gif", "png", "jpeg"};

		 // 파일 용량 확인 (1MB 이상일 경우 미리보기 안됨)
		 if(Arrays.asList(filetypeList).contains(filetype)) {
			 if(0 < fileSize && fileSize < 1000000) {
				 readAble = "y";
			 }
		 }

		 FileDetail fileDetail = new FileDetail(0, originalFilename, filename, filetype, path, readAble);

		 return fileDetail;
	 }


	 /** 4. 파일 다운로드  */
	 @Override
	 public void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
				HttpServletResponse response) throws Exception {

		 /* 1. 업로드 파일 다운로드 */
		 if(model.containsKey("fileDownload")) {
			 FileDetail fileDetail = (FileDetail) model.get("fileDownload");

			 // 접근 파일 경로
			 String filePath =  fileDetail.getFilePath() + "/" +fileDetail.getFile_name(); // 파일을 읽기위한 파일 경로
			 // 응답 옵션 설정 (인코딩,파일타입,MIME Type 등)
			 prepareResponse(request, response, fileDetail.getOriginal_name(), "application/octet-stream");

			 // 파일을 읽고, 출력한다.
			 downloadStream(response,filePath);


		/* 2. DB Data를 CSV 확장자로 다운로드 */
		}else if(model.containsKey("dataToCsv")) {

			List<FileDetail> fileList = (List<FileDetail>) model.get("dataToCsv");
			// 응답 옵션 설정 (인코딩,파일타입,MIME Type 등)
			prepareResponse(request, response, "파일업로드내역.csv", "text/csv; charset=UTF-8");
			downloadCsv(response,fileList);

		/* 3. img -> pdf 로 변환 */
		}else if(model.containsKey("imgToPdf")) {

			FileDetail fileDetail = (FileDetail) model.get("imgToPdf");
			 // 응답 옵션 설정 (인코딩,파일타입,MIME Type 등)
			prepareResponse(request,response,fileDetail.getOriginal_name().replace(fileDetail.getFiletype(), "pdf"),"application/pdf");

			downloadPdf(response,fileDetail);
		/* 4. pdf -> img 변환 */
		}else if(model.containsKey("pdfToImg")) {
			FileDetail fileDetail = (FileDetail) model.get("pdfToImg");
			String content = downloadImg(response,fileDetail);

			/* 파일 압축 처리
			 * 이미지 파일이 1개 이상이라면 zip파일로 압축한다 */
			if(content != null) { // 단일 이미지 파일
				String original_name = fileDetail.getOriginal_name();
				prepareResponse(request,response,original_name.replace(fileDetail.getFiletype(), "png"),"application/octet-stream");
				downloadStream(response,content);
			}
		}
	 }


     /** 5. 응답헤더 설정 */
	 protected void prepareResponse(HttpServletRequest request, HttpServletResponse response, String original_name, String contentType) throws Exception {
		 try {
			 // 1. 브라우저, 운영체제정보에 따른 파일 이름 인코딩 설정
			 String downloadFileName = "";
			 String userBrowser = request.getHeader("User-Agent");

			 // IE
			 if(userBrowser.contains("Trident") || userBrowser.contains("MSIE")) {
				 downloadFileName = URLEncoder.encode(original_name, "UTF-8").replaceAll("/+", " ");

			 // Chorme 등
			 }else {
				downloadFileName = new String(original_name.getBytes("UTF-8"), "ISO-8859-1");
			 }

			 // 2. 읽어온 파일 정보를 화면에서 다운로드 할 수 있게 변환 설정
			 response.setContentType(contentType);  // MIME Type

			 // 3. 다운로드 옵션 설정 : 데이터 형식, 첨부파일, 다운로드 되는 파일 이름
			 response.setHeader("Content-Disposition", "attachment; fileName=\"" + downloadFileName +"\";");

			 // 4. 전송되는 데이터의 안의 내용물들의 인코딩 방식
			 response.setHeader("Content-Transfer-Encoding", "binary");
		 } catch (UnsupportedEncodingException e) {
			 throw new IOException(e.getLocalizedMessage());
		 }
	 }


	 /** 6. 파일 경로를 읽어들여 다운로드 방식 */
	 protected void downloadStream(HttpServletResponse response,String filePath) throws Exception {
		 try {
			 // 바이트 단위로 파일을 읽어들인다.
			 FileInputStream fileInputStream = new FileInputStream(new File(filePath));

			 BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

			 // 지정한 InputStream의 내용을 지정한 OutputStream에 복사하고, 스트림을 닫는다. 리턴값 : copy한 byte 수
			 OutputStream outputStream = response.getOutputStream();

			 // 스프링 프레임워크에서 제공하는 파일 다운로드 기능 (while,flush,close)
			 FileCopyUtils.copy(bufferedInputStream, outputStream);

			 if(fileInputStream != null) {
				 fileInputStream.close();
			 }
			 bufferedInputStream.close();
			 outputStream.close();
		 } catch (FileNotFoundException e) {
			 System.out.println(e.getMessage());
			 throw new BadRequestException();
		 } catch (UnsupportedEncodingException e) {
			 System.out.println(e.getMessage());
			 throw new IOException(e.getLocalizedMessage());
		 }
	}


	/** 7. CSV 생성 */
	public void downloadCsv(HttpServletResponse response,List<FileDetail> fileList) throws IOException {

		try {
			// 1. CSV 파일에 데이터를 쓰기위한 CSVWriter인스턴스를 만든다.
	        CSVWriter csvWriter = new CSVWriter(response.getWriter(),
				        				CSVWriter.DEFAULT_SEPARATOR,
				        				CSVWriter.NO_QUOTE_CHARACTER,
				        				CSVWriter.DEFAULT_ESCAPE_CHARACTER,
				        				CSVWriter.DEFAULT_LINE_END);

			// 2. openCSV 가 제공하는 고유 한 매핑 설정을하는 인스턴스를 만든다.
	    	CustomMappingStrategy<FileDetail> strategy = new CustomMappingStrategy<>();

	    	// 3. 매핑되는 클래스 유형을 설정한다.
	    	strategy.setType(FileDetail.class);

	        // 4. 적용할 매핑 전략을 매개변수로 하여 StatefulBeanToCsvBuilder 클래스의 build 메소드를 호출하여 StatefulBeanToCsv 클래스의 객체를 생성한다
	    	//    - StatefulBeanToCsvBuilder 빌더 : CSV 파일 작성에 필요한 모든 매개 변수를 설정한다.
	    	StatefulBeanToCsv<FileDetail> beanToCsv = new StatefulBeanToCsvBuilder<FileDetail>(csvWriter).withMappingStrategy(strategy).build();

	        // 5. CSV 형식으로 빈을 작성
	        beanToCsv.write(fileList);

		}catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException  e) {
			System.out.println(e.getMessage());
            throw new IOException(e.getLocalizedMessage());
        }
	}


	/** 8. img -> pdf 생성 */
	public void downloadPdf(HttpServletResponse response,FileDetail fileDetail) throws IOException {

		// 1. 일정한 크기의 문서를 생성한다.
		Document document = new Document();
		PdfPTable table = null;
		OutputStream out = response.getOutputStream();
		try {
			// 쿠키를 생성한다.
			makeCookie(response);
			// 1. 파일명을 매개변수로 이미지 객체를 생성하여 문서에 삽입해야 하는 그래픽 요소(JPEG, PNG 또는 GIF)를 나타낸다.
			Image img = Image.getInstance(fileDetail.getFilePath()+"/"+fileDetail.getFile_name());

			// 2. PDF 문서보다 이미지 크기가 더 클경우 문서크기에 맞춰서 조정
			Rectangle documentRect = document.getPageSize();

			if (img.getWidth() > documentRect.getWidth() || img.getHeight() > documentRect.getHeight()) {
			    Rectangle rectangle = new Rectangle(img.getWidth(),img.getHeight());
			    // 문서 크기를 이미지 크기로 지정
			    document = new Document(rectangle);
			    // 이미지 비율 조정을 위해  PdfPTable 객체 생성
			    table = new PdfPTable(1);
			    // PdfPCell : 이미지를 셀에 맞추는 속성을 true로 설정.
			    table.addCell(new PdfPCell(img,true));
			}

			/* 3. 문서객체 와 출력스트림를 매개변수로 PDFWriter 클래스에 전달하여 PdfWriter 객체를 생성한다.
		 	문서에 요소를 추가시 PDF 파일에  쓰여진다.*/
		    PdfWriter.getInstance(document,out);

		    // 4. 문서 오픈
		 	document.open();

			// 5. 문서에 생성한 이미지 객체 추가
		    if(table != null) {
		    	document.add(table);
		    }else {
		    	document.add(img);
		    }
			/* 6. 문서를 닫는다.
			문서에 기록된 모든 요소가 플러시되어 PDF 파일에 기록된다. */
		    document.close();
		    out.close();
		} catch (DocumentException e) {
			System.out.println(e.getMessage());
			throw new IOException(e.getLocalizedMessage());
		}
	}


	/** 9. pdf-> img 생성 */
	public String downloadImg(HttpServletResponse response,FileDetail fileDetail) throws IOException {
		String result_path = null; // 반환 경로
		try {
			// 1. 요청받은 파일 객체를 매개변수로 받아서 PDF 문서를 로드한다.
			PDDocument pdDocument = PDDocument.load(new File(fileDetail.getFilePath()+"/"+fileDetail.getFile_name()));
			/* 2. 위에서 로드한 PDF 문서객체를 전달하여 PDFRenderer클래스 객체 생성 한다.
			 *    PDFRenderer 클래스는 PDF 문서를 AWT BufferedImage 로 렌더링하는 기능이 있다. */
			PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
			// 3. 변환된 이미지 경로를 담는 List 객체
			List<String> savedImgList = new ArrayList<>();

			// 4. 생성되는 이미지 파일 이름
			String imgFileName = "";

			// 5. 반복문을 통해 pdf 전체 페이지를 읽으면서 이미지 파일로 변환한다.
			for (int i = 0; i < pdDocument.getNumberOfPages(); i++) {

				// 5-1. 이미지 파일 이름 설정
				String filename = fileDetail.getOriginal_name().substring(0, fileDetail.getOriginal_name().indexOf("."));
				imgFileName = filename +"(" +i +")" +".png";

				/* 5-2. PDF 문서 페이지의 인덱스를 전달 하여 특정 페이지의 이미지를 렌더링한다.
				   		매개변수: renderImageWithDPI (페이지 인덱스, 렌더링할 DPI, 이미지 유형) */
				BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(i, 300, ImageType.RGB);

				/* 5-3. 랜더링된 이미지를 파일에 쓴다.
				     	매개변수: writeImage(쓸 이미지, 파일이름, 해상도) */
	            ImageIOUtil.writeImage(bufferedImage, imgFileName , 300);

	            // 5-4. 저장 완료된 이미지를 list에 추가한다.
	            savedImgList.add(imgFileName);
			}

			makeCookie(response);

			// 6. pdf 페이지가 여러개 일 경우 zip 압축
			if( pdDocument.getNumberOfPages() > 1) {
				createZip(savedImgList, response);

			// 7. pdf 페이지가 한개 일 경우 png로 반환
			}else {
				result_path = imgFileName;
			}

			// 8. PDF 문서 닫기
			pdDocument.close();

		} catch (InvalidPasswordException e) {
			System.out.println(e.getMessage());
			throw new IOException(e.getLocalizedMessage());
		}
		return result_path;
	}


	/** 10. zip 압축 */
	public void createZip(List<String> savedImgList,HttpServletResponse response) throws IOException {

		OutputStream  outputStream  = response.getOutputStream();
		// 1. ZIP 파일 형식으로 파일을 쓰기 위한 출력 스트림 생성
		ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
		try {
			// 2. 이미지 파일로 변환한 파일 경로 리스트의 요소들을 반복문을 돌면서 접근한다.
			for (int i = 0; i < savedImgList.size(); i++) {

				String filename = savedImgList.get(i);

				// 2-1. 해당 경로의 파일을 바이트 스트림으로 읽는다.
				FileInputStream inputStream = new FileInputStream(filename);

				// 2-2. 경로에서  파일명 추출
				//String filename = savedImgList.get(i).substring(savedImgList.get(i).lastIndexOf("\\")+1);

				// 2-3. 매개변수로 받은 파일명으로 새 ZIP 파일 항목을 만든다.
				ZipEntry zipEntry = new ZipEntry(filename);

				/* 2-4. ZIP 파일 출력 스트림에 Zip 항목을 쓴다.
				 	ex. Zip[zipEntry, zipEntry, zipEntry] */
				zipOutputStream.putNextEntry(zipEntry);

				// 2-5. 지정된 버퍼로 파일을 읽고, 현재 ZIP 항목 데이터에 바이트 배열을 쓴다.
				int length;
				byte[] buffer = new byte[1024];  // 제일 최대값으로 설정
				/** - 입력 스트림으로부터 매개값으로 주어진 바이트 배열의 길이만큼 바이트를 읽고 저장하고 읽은 바이트 수를 리턴.
				*   - 입력 스트림으로부터 더 이상 바이트를 읽을 수 없으면  -1 리턴
				*/
				while ((length = inputStream.read(buffer)) > 0 ) {
					/** 현재 zip 입력 데이터 바이트의 배열을 기입 한다.
					        매개변수 : write(데이터가 기록될 버퍼, 목적지 배열의 시작 오프셋 b, 쓸 바이트 수) **/
					zipOutputStream.write(buffer, 0, length);
				}
				inputStream.close();
	        }

			// 3. 압축된 출력 스트림을 플러시한다.
			zipOutputStream.flush();

			// 4. 현재 ZIP 항목을 닫고 다음 항목을 쓰기 위해 스트림을 배치한다.
			zipOutputStream.closeEntry();

			// 5. ZIP 출력 스트림과 필터링되는 스트림을 닫는다.
			zipOutputStream.close();
		} finally {
			outputStream.close();
		}
	}


	/** 11. 파일 확장자 변환을 위해 임시로 저장한 파일 삭제 */
	public void deleteFile() throws IOException {
		String path = setFilePath("convert");
		File file = new File(path);
		if(file.exists()) {
			System.out.println("path:" + path);
			FileUtils.deleteDirectory(file);
		}
	}


	/** 12. summernote 이미지 삭제*/
	public boolean deleteSummernoteImg(String imgstr) throws Exception {
		boolean result = true;

		// 이미지 태그를 정규식을 이용하여 추출
		Pattern imgPattern = Pattern.compile("<img[^>]*src=[\"']?([^>\"']+)[\"']?[^>]*>");

		// 정규식 패턴을 target(게시글 내용)과 매치하여 이미지 태그가 있는지 확인
		Matcher matcher = imgPattern.matcher(imgstr);

		List<String> imgPathList = new ArrayList<String>();

		// 반복문을 돌려서 정규식과 매치하는 태그가 존재하는지 검사
		while (matcher.find()) {
			// 본문글에 이미지가 1장이상 일 수도 있으므로 리스트에 저장한다.
			imgPathList.add(matcher.group(1));
		}

		String real_path = servletContext.getRealPath("/");
		String resoure_path = "resources/img/upload/board/";

		for(String imgPath: imgPathList) {
			File file = new File(real_path + resoure_path + imgPath);
			if(file.exists()) {
				result = file.delete();
				if(result == false) {
					throw new BadRequestException();
				}
			} else {
				throw new  FileNotFoundException();
			}
		}
		return result;
	}

	/** 13. 쿠키 생성 */
	public void makeCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("fileDownloadToken", "TRUE");
		cookie.setPath("/");
		response.addCookie(cookie);
	}
}






