package server.spring.file.aws.controller;


import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import server.spring.file.aws.controller.dto.AwsS3LogDTO;
import server.spring.file.aws.controller.dto.AwsS3Request;
import server.spring.file.aws.domain.enums.AwsS3Target;
import server.spring.file.aws.service.AwsS3Service;
import server.spring.file.common.dto.Response;
@RestController
@RequestMapping("/aws/s3")
@RequiredArgsConstructor
@Slf4j
public class AwsS3Controller {
    private final AwsS3Service awsS3Service;


    @PostMapping(
        value = "/upload",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Response<AwsS3LogDTO> put(
        @RequestPart MultipartFile multipartFile, @RequestParam String path) {
        AwsS3Request request = new AwsS3Request(AwsS3Target.DIRECT, multipartFile, multipartFile.getOriginalFilename(), path);

        AwsS3LogDTO awsS3LogDTO =
            awsS3Service.uploadFileToS3bucket(request);

        return new Response<>(awsS3LogDTO);
    }


    @GetMapping("/list")
    public Response<AwsS3LogDTO.AwsS3Data> getList(AwsS3Request request) {
        AwsS3LogDTO.AwsS3Data data = awsS3Service.getFileList(request);
        return new Response<>(data);
    }


    @PostMapping(value = "/download")
    public Response<S3Object> download(@RequestBody AwsS3Request request) {
        S3Object s3Object = awsS3Service.downloadFileFromS3bucket(request);

        return new Response<>(s3Object);
    }

    @PostMapping(value = "/delete")
    public Response<AwsS3LogDTO> delete(@RequestBody AwsS3Request request) {
        AwsS3LogDTO awsS3LogDTO =
                awsS3Service.deleteFileFromS3bucket(request);

        return new Response<>(awsS3LogDTO);
    }
}
