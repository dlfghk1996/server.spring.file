package server.spring.file.common.config.aws;




import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "cloud.aws")
public class AwsProperties {
    private final Credentials credentials = new Credentials();
    private final S3 s3 = new S3();

    @Value("${cloud.aws.region.static}")
    private String region;

    @Data
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }

    @Data
    public static class S3 {
        private String bucket;
        private String domain;
        private Boolean use;
        private Boolean thumbnail;
    }

    @Bean
    public AmazonS3Client amazonS3Client() {

        BasicAWSCredentials awsCredentials =
            new BasicAWSCredentials(credentials.getAccessKey(), credentials.getSecretKey());
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
            .build();
    }
}
