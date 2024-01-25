package server.spring.file.aws.service;


import server.spring.file.aws.controller.dto.AwsS3LogDTO;
import server.spring.file.aws.domain.AwsS3Log;

public interface AwsS3LogService  {

	AwsS3LogDTO addLog(AwsS3Log awsS3Log);
}
