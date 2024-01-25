package server.spring.file.reader.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class OrderWeekPoint {

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "is_mon")
    private Integer isMon;

    @CsvBindByName(column = "is_tue")
    private Integer isTue;

    @CsvBindByName(column = "is_wed")
    private Integer isWed;

    @CsvBindByName(column = "is_thu")
    private Integer isThu;

    @CsvBindByName(column = "is_fri")
    private Integer isFri;

    @CsvBindByName(column = "is_sat")
    private Integer isSat;

    @CsvBindByName(column = "is_sun")
    private Integer isSun;

}
