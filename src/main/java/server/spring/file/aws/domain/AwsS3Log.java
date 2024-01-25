

package server.spring.file.aws.domain;
import lombok.Builder;
import java.util.Date;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import server.spring.file.aws.domain.enums.AwsLogResultType;
import server.spring.file.aws.domain.enums.AwsMethodType;
import server.spring.file.aws.domain.enums.AwsS3Target;


@Getter
@Setter
@RequiredArgsConstructor
// @Entity
public class AwsS3Log {

  private Long id;


  //@Enumerated(EnumType.STRING)
  // ENUM('DIRECT','HOT_RECIPE','BANNER','PRODUCT_IMAGE')
  private AwsS3Target target;

  // 종류
  //@Enumerated(EnumType.STRING)
  // ENUM('PUT','DELETE','UPDATE','DOWNLOAD')
  private AwsMethodType methodType;

  // 파일 이름
  private String originalFileName;

  // 경로
  private String path;

  private String contextType;

  // 마지막 수정 날짜
  private Date lastModified;

  private String uri;

  // 결과
  //@Enumerated(EnumType.STRING)   // ENUM('SUCCESS','FAIL')
  private AwsLogResultType result;


}
