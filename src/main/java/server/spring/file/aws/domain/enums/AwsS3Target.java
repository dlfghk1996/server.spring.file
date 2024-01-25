package server.spring.file.aws.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AwsS3Target  {
  DIRECT("DIRECT", "직접 호출"),
  HOT_RECIPE("MAIN", "메인"),
  BANNER("PROFIL", "프로필");

  @JsonValue private final String code;
  private final String label;

  AwsS3Target(String code, String label) {
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
