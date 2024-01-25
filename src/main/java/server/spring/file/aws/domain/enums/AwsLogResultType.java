package server.spring.file.aws.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
public enum AwsLogResultType {
	SUCCESS("SUCCESS", "성공"),
	FAIL("FAIL", "실패");

	@JsonValue
	private final String code;
	private final String label;

	AwsLogResultType(String code, String label) {
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
