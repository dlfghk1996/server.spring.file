package server.spring.file.aws.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AwsMethodType {
	PUT("PUT", "추가"),
	DELETE("DELETE", "삭제"),
	UPDATE("UPDATE", "수정"),
	DOWNLOAD("DOWNLOAD", "다운로드");

	@JsonValue
	private final String code;
	private final String label;

	AwsMethodType(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return this.code;
	}


	public String getLabel() {
		return this.label;
	}
}
