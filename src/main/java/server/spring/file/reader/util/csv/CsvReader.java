package server.spring.file.reader.util.csv;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import server.spring.file.reader.dto.OrderWeekPoint;
import server.spring.file.reader.dto.FoodGroup;

@Data
@Component
public class CsvReader {

    public static List<OrderWeekPoint> ORDERWEEKPOINT;

    public static Map<String, Map<String, String>> ORDERPOINT;     //<주문-점수, <브랜드,점수>>

    public static Map<String, List<FoodGroup>> FOODGROUP;

    public static LinkedHashMap<Pair, LinkedHashMap<String,ArrayList<FoodGroup>>> FOODGROUPMAP;


    public CsvReader() {
        try {
            // 파일의  charset을 알아낸다.
            // 파일의 charset 형식으로 파일을 읽어낸다.
            //    Reader reader = Files.newBufferedReader(Paths.get(localPath + "affiliations.csv"));

            this.ORDERWEEKPOINT = readOrderWeekPointFile();
            this.ORDERPOINT = readOrderPointFile();
            this.FOODGROUP = convertFoodGroupMapByFood();
            this.FOODGROUPMAP = convertFoodGroupByRegion();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static List<OrderWeekPoint> readOrderWeekPointFile() throws IOException {

        ClassPathResource resource = new ClassPathResource("csv/order_week_point.csv");
        return new CsvToBeanBuilder<OrderWeekPoint>(
                new BufferedReader(new InputStreamReader(resource.getInputStream(),"euc-kr")))
                .withType(OrderWeekPoint.class)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .withEscapeChar('\n')
                .build()
                .parse();
    }


    public static Map<String, Map<String, String>> readOrderPointFile() throws IOException, CsvValidationException {

            ClassPathResource resource = new ClassPathResource("csv/order_point.csv");
            CSVReaderHeaderAware csvReaderHeaderAware = new CSVReaderHeaderAware(
                     new BufferedReader(new InputStreamReader(resource.getInputStream(), "euc-kr")));
            Map<String, String> values = new LinkedHashMap<>();

            Map<String, Map<String, String>> map = new LinkedHashMap<>();

            while ((values = csvReaderHeaderAware.readMap()) != null) {
                map.put(values.get("주문-점수"), values);
            }
            return map;
    }


    // 브랜드별 map
    private static LinkedHashMap<String, List<FoodGroup>> convertFoodGroupMapByFood() throws IOException {

        ClassPathResource resource = new ClassPathResource("csv/food_group.csv");
        LinkedHashMap<String, List<FoodGroup>> map;
        List<FoodGroup> foodGroupList =  new CsvToBeanBuilder<FoodGroup>(new BufferedReader(new InputStreamReader(resource.getInputStream(),"euc-kr")))
                .withType(FoodGroup.class)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .withEscapeChar('\n')
                .build()
                .parse();
        // 정렬
        map = foodGroupList.stream()
                .sorted(Comparator.comparing(FoodGroup::getRanking))
                .collect(groupingBy(FoodGroup::getFood,LinkedHashMap::new, Collectors.toList()));

        return map;
    }

    // 브랜드-지역별 map
    private static LinkedHashMap<Pair, LinkedHashMap<String,ArrayList<FoodGroup>>> convertFoodGroupByRegion() throws IOException {

        ClassPathResource resource = new ClassPathResource("csv/food_group.csv");
        List<FoodGroup> list =  new CsvToBeanBuilder<FoodGroup>(new BufferedReader(new InputStreamReader(resource.getInputStream(),"euc-kr")))
                .withType(FoodGroup.class)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .withEscapeChar('\n')
                .build()
                .parse();

        // <<브랜드-지역, ranking>,<메뉴, 리스트>>
        LinkedHashMap<Pair, LinkedHashMap<String,ArrayList<FoodGroup>>> result =
                list.stream()
                        .sorted(comparing(FoodGroup::getRanking)
                                .thenComparing(FoodGroup::getProperScore))
                        .collect(
                            groupingBy(foodGroup -> new ImmutablePair<>(
                                foodGroup.getName()+"-"+foodGroup.getRegion(), foodGroup.getRanking()),
                                LinkedHashMap::new,
                                groupingBy(FoodGroup::getFood,LinkedHashMap::new, Collectors.toCollection(ArrayList::new))));

        return result;
    }
}

