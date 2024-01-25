package server.spring.file.aws.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import server.spring.file.aws.domain.enums.AwsS3Target;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class  AwsS3Request{
    private AwsS3Target target;
    private MultipartFile file;
    private String originFileName;
    private String path;
}
