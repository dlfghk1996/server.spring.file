package server.spring.file.common.enums;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
public enum ResponseCode {

  /** ** [ 1000 ~ 1999 ] : SUCCESS *** */
  OK(0, "success", SC_OK),
  SUCCESS(1000, "success", SC_OK),

  INVALID_PARAMETERS(4000, "유효하지 않은 인자 값 입니다.", SC_BAD_REQUEST),
  RESULT_NOT_FOUND(4004, "결과가 없습니다.", SC_NO_CONTENT),

  /* [5000 - 5999] : point */
  POINT_ARE_NOT_AVAILABLE(5000, "사용 가능한 포인트가 없습니다.", SC_OK),

  FILE_UPLOAD_FAIL(6001, "파일 업로드에 실패했습니다.", SC_INTERNAL_SERVER_ERROR),

  /** ** [ 9000 - 9009 ] : ERROR *** */
  ERROR(9000, "ERROR", SC_OK),
  UNKNOWN(9999, "알 수 없는 오류입니다.", SC_INTERNAL_SERVER_ERROR);

  private final int code;
  private final String label;
  private final int httpStatusCode;

  ResponseCode(int code, String label, int httpStatusCode) {
    this.code = code;
    this.label = label;
    this.httpStatusCode = httpStatusCode;
  }

  public int getCode() {
    return this.code;
  }

  public String getLabel() {
    return this.label;
  }

  public int getHttpStatusCode() {
    return this.httpStatusCode;
  }
}
