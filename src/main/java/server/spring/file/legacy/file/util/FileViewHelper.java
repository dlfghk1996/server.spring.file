package server.spring.file.legacy.file.util;

import java.util.List;
import server.spring.file.legacy.file.excel.SheetHelper;

/** 읽은 파일의 내용을 담기위한 파일 타입별 객체 설정 */
public class FileViewHelper {
	private List<SheetHelper> sheetHelper; //.xls, xlxs 데이터
	private List<List<String>> excelList;   //.xls, xlxs 데이터
	private List<String[]> csvList;         // .csv 파일
	private List<String> txtList;           // .txt 파일
	private String sourcePath;              // img, pdf
	private String fileType;


	// get,set
	public List<List<String>> getExcelList() {
		return excelList;
	}
	public void setExcelList(List<List<String>> excelList) {
		this.excelList = excelList;
	}
	public List<SheetHelper> getSheetHelper() {
		return sheetHelper;
	}
	public void setSheetHelper(List<SheetHelper> sheetHelper) {
		this.sheetHelper = sheetHelper;
	}
	public List<String[]> getCsvList() {
		return csvList;
	}
	public void setCsvList(List<String[]> csvList) {
		this.csvList = csvList;
	}
	public List<String> getTxtList() {
		return txtList;
	}
	public void setTxtList(List<String> txtList) {
		this.txtList = txtList;
	}
	public String getSourcePath() {
		return sourcePath;
	}
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
}
