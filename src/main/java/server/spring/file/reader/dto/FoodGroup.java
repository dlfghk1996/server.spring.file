package server.spring.file.reader.dto;

import com.opencsv.bean.CsvBindByName;
import java.math.BigDecimal;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class FoodGroup {

    @CsvBindByName(column = "code")
    private String code;

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "menu")
    private String menu;

    @CsvBindByName(column = "region")
    private String region;

    @CsvBindByName(column = "food")
    private String food;

    @CsvBindByName(column = "proper_score")
    private int properScore;

    @CsvBindByName(column = "ranking")
    private int ranking;



}
