package server.spring.file.reader.service;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import server.spring.file.reader.dto.ExcelSampleDto;
import server.spring.file.reader.util.ExcelUtil;

// 대용량
@Service
public class ExcelSampleService {

    @Autowired
    ExcelUtil excelUtil;

    public List<ExcelSampleDto> uploadExcel(MultipartFile file) {
        return excelUtil.excelSampleUploadService(file);
    }

    public List<ExcelSampleDto> uploadExcelBySax(MultipartFile file) {
        return excelUtil.excelSampleUploadServiceBySax(file);
    }
}

