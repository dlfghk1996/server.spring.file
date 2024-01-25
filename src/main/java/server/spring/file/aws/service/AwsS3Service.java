package server.spring.file.aws.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import server.spring.file.aws.controller.dto.AwsS3LogDTO;
import server.spring.file.aws.controller.dto.AwsS3Request;
import server.spring.file.aws.domain.AwsS3Log;
import server.spring.file.aws.domain.enums.AwsLogResultType;
import server.spring.file.aws.domain.enums.AwsMethodType;
import server.spring.file.common.config.aws.AwsProperties;
import net.coobird.thumbnailator.Thumbnails;


@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

	private final AmazonS3 amazonS3;
	private final AwsProperties awsProperties;
	private final AwsS3LogService awsS3LogService;

	private static String DEFAULT_PATH = "/default";

	public AwsS3LogDTO uploadFileToS3bucket(AwsS3Request request) {
		String path = request.getPath();
		String filename = request.getOriginFileName();

		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentType(
			MediaTypeFactory.getMediaType(request.getFile().getOriginalFilename())
				.orElse(MediaType.APPLICATION_OCTET_STREAM)
				.toString());

		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		AwsS3Log awsS3Log = this.setAWSLog(request, AwsMethodType.PUT);

		try {
			amazonS3.putObject(
				new PutObjectRequest(
					awsProperties.getS3().getBucket() + path, filename, request.getFile().getInputStream(), meta)
					.withCannedAcl(CannedAccessControlList.PublicRead));

			S3Object s3Object = amazonS3.getObject(awsProperties.getS3().getBucket() + path, filename);

			awsS3Log = this.setResponseAWSLog(awsS3Log, s3Object);

		} catch (UnsupportedEncodingException e) {
			log.error("Filename encoding error... ", e);
			awsS3Log.setResult(AwsLogResultType.FAIL);
			awsS3LogService.addLog(awsS3Log);
			// 예외 처리
		} catch (IOException e) {
			log.error("이미지 처리 오류 - " + e.getLocalizedMessage(), e.getCause());
			awsS3Log.setResult(AwsLogResultType.FAIL);
			awsS3LogService.addLog(awsS3Log);
			// 예외 처리
		} catch (Exception e) {
			log.error("err: ", e);
			awsS3Log.setResult(AwsLogResultType.FAIL);
			awsS3LogService.addLog(awsS3Log);
			// 예외 처리
		}

		return awsS3LogService.addLog(awsS3Log);
	}

	// TODO 디버깅 필요
	public S3Object downloadFileFromS3bucket(AwsS3Request request) {
		String path = request.getPath();
		String filename = request.getOriginFileName();

		log.info(
			"bucket : "
				+ awsProperties.getS3().getBucket()
				+ Optional.ofNullable(path).orElse(DEFAULT_PATH));

		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		AwsS3Log awsS3Log = this.setAWSLog(request, AwsMethodType.DOWNLOAD);


		S3Object s3Object = null;

		try {
			s3Object =
				amazonS3.getObject(
					awsProperties.getS3().getBucket() + Optional.ofNullable(path).orElse(DEFAULT_PATH), filename);

			this.setResponseAWSLog(awsS3Log, s3Object);

			awsS3LogService.addLog(awsS3Log);
		} catch (Exception e) {
			log.error("err: ", e);
			awsS3Log.setResult(AwsLogResultType.FAIL);
			awsS3LogService.addLog(awsS3Log);
			// 예외 처리
		}
		return s3Object;
	}

	public AwsS3LogDTO deleteFileFromS3bucket(AwsS3Request request) {
		String path = request.getPath();
		String filename = request.getOriginFileName();

		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		AwsS3Log awsS3Log = this.setAWSLog(request, AwsMethodType.DELETE);
		try {
			S3Object s3Object =
				amazonS3.getObject(
					awsProperties.getS3().getBucket() + Optional.ofNullable(path).orElse(DEFAULT_PATH), filename);

			amazonS3.deleteObject(
				awsProperties.getS3().getBucket() + Optional.ofNullable(path).orElse(DEFAULT_PATH), filename);

			this.setResponseAWSLog(awsS3Log, s3Object);

		} catch (Exception e) {
			log.error("err: ", e);
			awsS3Log.setResult(AwsLogResultType.FAIL);
			awsS3LogService.addLog(awsS3Log);
			// 예외 처리
		}

		return awsS3LogService.addLog(awsS3Log);
	}

	// TODO 디버깅 필요
	public AwsS3LogDTO.AwsS3Data getFileList(AwsS3Request request) {
		String path = request.getPath();
		AwsS3LogDTO.AwsS3Data awsS3Data = new AwsS3LogDTO.AwsS3Data();
		awsS3Data.setPath(path);

		List<AwsS3LogDTO.AwsS3Data.AwsS3Object> objects = new ArrayList<>();

		boolean bucketNameSetting = false;

		ListObjectsRequest listObject = new ListObjectsRequest();
		listObject.setBucketName(awsProperties.getS3().getBucket());
		listObject.setPrefix(path);

		ObjectListing objectListing = amazonS3.listObjects(listObject);

		String domain = awsProperties.getS3().getDomain();

		do {
			if (objectListing.getObjectSummaries().size() > 1) {
				for (int i = 1; i < objectListing.getObjectSummaries().size(); i++) {
					S3ObjectSummary s3ObjectSummary = objectListing.getObjectSummaries().get(i);

					if (!bucketNameSetting) {
						bucketNameSetting = true;
						awsS3Data.setBucketName(s3ObjectSummary.getBucketName());
					}

					AwsS3LogDTO.AwsS3Data.AwsS3Object object = new AwsS3LogDTO.AwsS3Data.AwsS3Object();
					object.setUrl(domain + this.fileNameEncoder(s3ObjectSummary.getKey()));
					object.setLastModified(s3ObjectSummary.getLastModified());
					object.setSize(s3ObjectSummary.getSize());
					object.setStorageClass(s3ObjectSummary.getStorageClass());

					objects.add(object);
				}
			}
			listObject.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());

		awsS3Data.setObjects(objects);

		return awsS3Data;
	}

	private String fileNameEncoder(String fileName) {
		String[] splitSlash = fileName.split("/");

		StringBuilder result = new StringBuilder();

		for (String s : splitSlash) {
			result.append("/").append(URLEncoder.encode(s));
		}

		return result.toString();
	}

	public String getDomain() {
		return awsProperties.getS3().getDomain();
	}

	private BufferedImage makeThumbnail(InputStream is) throws IOException {
		BufferedImage image = Thumbnails.of(is).scale(1).asBufferedImage();

		BufferedImage thumbnail =
			Thumbnails.of(image)
				.size(300, image.getHeight() * 300 / image.getWidth())
				.asBufferedImage();

		return thumbnail;
	}

	private AwsS3Log setAWSLog(AwsS3Request request, AwsMethodType awsMethodType){
		AwsS3Log awsS3Log = new AwsS3Log();
		awsS3Log.setOriginalFileName(request.getOriginFileName());
		awsS3Log.setPath(request.getPath());
		awsS3Log.setMethodType(awsMethodType.PUT);
		awsS3Log.setTarget(request.getTarget());
		return awsS3Log;
	}

	private AwsS3Log setResponseAWSLog(AwsS3Log awsS3Log, S3Object s3Object){
		awsS3Log.setUri(s3Object.getObjectContent().getHttpRequest().getURI().toString());
		awsS3Log.setLastModified(s3Object.getObjectMetadata().getLastModified());
		awsS3Log.setContextType(s3Object.getObjectMetadata().getContentType());
		awsS3Log.setResult(AwsLogResultType.SUCCESS);

		return awsS3Log;
	}
}
