package server.spring.file.aws.controller.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import server.spring.file.aws.domain.enums.AwsLogResultType;
import server.spring.file.aws.domain.enums.AwsMethodType;
import server.spring.file.aws.domain.enums.AwsS3Target;

@Data
public class AwsS3LogDTO {
    private Long id;

    // DIRECT / HOT_RECIPE / BANNER / PRODUCT_IMAGE
    private AwsS3Target target;

    // PUT / DELETE / UPDATE / DOWNLOAD
    private AwsMethodType methodType;

    // 파일 이름
    private String originalFileName;

    // 경로
    private String path;

    private String contextType;

    //마지막 수정 시간")
    private Date lastModified;


    private String uri;

    //SUCCESS / FAIL
    private AwsLogResultType result;


    @Data
    public static class AwsS3LogRequest{
        private Long id;

        // example = "DIRECT / HOT_RECIPE / BANNER
        private AwsS3Target target;

        // example = "PUT / DELETE / UPDATE / DOWNLOAD")
        private AwsMethodType methodType;

        private String originalFileName;

        private String path;

        private String contextType;

        private Date lastModified;

        private String uri;

        private AwsLogResultType result;
    }

    @Data
    public static class AwsS3Data {
        private String path;
        private String BucketName;
        private List<AwsS3Object> objects;

        @Data
        public static class AwsS3Object {
            private String url;

            private Long size;

            private Date lastModified;

            private String StorageClass;
        }
    }
}
