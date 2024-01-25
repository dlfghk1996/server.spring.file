package server.spring.file.aws.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import server.spring.file.aws.controller.dto.AwsS3LogDTO;
import server.spring.file.aws.domain.AwsS3Log;

@Slf4j
@RequiredArgsConstructor
@Service
public class AwsS3LogServiceImpl implements AwsS3LogService {

	private final ModelMapper mapper;
	@Override
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public AwsS3LogDTO addLog(AwsS3Log awsS3Log) {
		// TODO db 저장 로직
		return mapper.map(awsS3Log,AwsS3LogDTO.class);
	}
}
