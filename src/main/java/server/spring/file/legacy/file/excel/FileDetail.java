package server.spring.file.legacy.file.excel;


import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class FileDetail {

    // pk
	@CsvBindByName(column = "파일 번호")
	@CsvBindByPosition(position = 0)
	@ExcelHeaderColum(headerName = "파일 번호")
	private int file_num;

	// 기존  파일 이름
	@CsvBindByName(column = "파일명")
	@CsvBindByPosition(position = 1)
	@ExcelHeaderColum(headerName = "파일명")
	private String original_name;

	// 서버에  저장되는 파일이름
	private String file_name;

	// 파일 확장자
	@CsvBindByName(column = "확장자")
	@CsvBindByPosition(position = 2)
	@ExcelHeaderColum(headerName = "확장자")
	private String filetype;

	// 등록일
	@CsvBindByName(column = "등록일")
	@CsvBindByPosition(position = 3)
	@ExcelHeaderColum(headerName = "등록일")
	private String register_date;

	// 저장되어 있는 경로
	private String filePath;

	// 미리보기 기능을 지원하는 파일인지 구분
	private String readAble;

	// dto 에서만 사용
	private String tempFilename;

	private int rnum;

	// 기본 생성자
	public FileDetail() {
	}

	public FileDetail(int file_num, String original_name, String file_name, String filetype, String filePath, String readAble) {
		super();
		this.file_num = file_num;
		this.original_name = original_name;
		this.file_name = file_name;
		this.filetype = filetype;
		this.filePath = filePath;
		this.readAble = readAble;
	}

	// get/set
	public int getFile_num() {
		return file_num;
	}

	public void setFile_num(int file_num) {
		this.file_num = file_num;
	}

	public String getOriginal_name() {
		return original_name;
	}

	public void setOriginal_name(String original_name) {
		this.original_name = original_name;
	}

	public String getFile_name() {
		return file_name;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public String getFiletype() {
		return filetype;
	}

	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}

	public String getRegister_date() {
		return register_date;
	}

	public void setRegister_date(String register_date) {
		this.register_date = register_date;
	}

	public String getReadAble() {
		return readAble;
	}

	public void setReadAble(String readAble) {
		this.readAble = readAble;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getTempFilename() {
		return tempFilename;
	}

	public void setTempFilename(String tempFilename) {
		this.tempFilename = tempFilename;
	}

	public int getRnum() {
		return rnum;
	}

	public void setRnum(int rnum) {
		this.rnum = rnum;
	}

	@Override
	public String toString() {
		return "FileDetail [file_num=" + file_num + ", original_name=" + original_name + ", file_name=" + file_name
				+ ", filetype=" + filetype + ", register_date=" + register_date + ", filePath=" + filePath
				+ ", readAble=" + readAble + ", tempFilename=" + tempFilename + "]";
	}
}
