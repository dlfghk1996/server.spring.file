package server.spring.file.reader.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import server.spring.file.reader.dto.ExcelSampleDto;
import server.spring.file.common.dto.Response;
import server.spring.file.common.enums.ResponseCode;
import server.spring.file.reader.service.ExcelSampleService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileReaderController {

    private final ExcelSampleService excelSampleService;

    // 엑셀 업로드 테스트 API
    @PostMapping(
        value = "/upload",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Response<List<ExcelSampleDto>> uploadExcel(
        @RequestPart MultipartFile multipartFile) {
        List<ExcelSampleDto> dataList = excelSampleService.uploadExcel(multipartFile);

        return new Response<>(dataList, ResponseCode.OK);
    }


    // 엑셀 대용량 업로드 테스트 API
    @PostMapping(
        value = "/upload/sax",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Response<List<ExcelSampleDto>> uploadExcelBySax(
        @RequestPart MultipartFile multipartFile, @RequestParam String path) {
        List<ExcelSampleDto> dataList = excelSampleService.uploadExcelBySax(multipartFile);

        return new Response<>(dataList, ResponseCode.OK);
    }

}
