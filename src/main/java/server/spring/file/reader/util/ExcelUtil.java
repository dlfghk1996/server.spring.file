package server.spring.file.reader.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import server.spring.file.reader.dto.ExcelSampleDto;
import server.spring.file.reader.util.sax.ExcelSaxReader;
import server.spring.file.reader.util.workbook.ExcelWorkbookReader;


@Component
@Slf4j
public class ExcelUtil implements ExcelEventHandlerByRows {

    @Getter
    private List<ExcelSampleDto> dataList = new ArrayList<>();

    // 대용량
    public List<ExcelSampleDto> excelSampleUploadServiceBySax (MultipartFile file) {
        ExcelSaxReader<ExcelSampleDto> reader = new ExcelSaxReader<>(ExcelSampleDto.class, this);
        reader.readExcelBySerial(file);

      //  return this.dataList;
        return reader.getDataList();
    }

    public List<ExcelSampleDto> excelSampleUploadService(MultipartFile file) {
        ExcelWorkbookReader reader = new ExcelWorkbookReader(ExcelSampleDto.class);
        return reader.readExcel(file, ExcelSampleDto.class);
        //return this.dataList;
    }

    @Override
    public <T> void doNext(List<T> target, Map<T, String> errMap) {
        log.error(errMap.toString());
        log.info("target: {}", target.toString());
        target.forEach(t -> this.dataList.add((ExcelSampleDto) t));
    }

}
