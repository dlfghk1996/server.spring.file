package server.spring.file.reader.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import server.spring.file.reader.annotaion.ExcelColumn;

@Getter
@Setter
@ToString
public class ExcelSampleDto {
    @ExcelColumn(headerName = "columnA")
    private String columnA;
    @ExcelColumn(headerName = "columnB")
    private String columnB;
}
