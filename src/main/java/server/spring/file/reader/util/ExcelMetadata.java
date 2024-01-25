package server.spring.file.reader.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import server.spring.file.reader.annotaion.ExcelColumn;

public class ExcelMetadata<T> {
    @Getter
    private List<Field> headerNames = new ArrayList<>();// {columnA, columB}
    @Getter
    private Map<String, Field> columns = new LinkedHashMap<>(); // LinkedHashMap: key 의 순서를 보장 // // {columnA : columnA, columB:columnB}
    private Class<T> type;

    public ExcelMetadata(Class<T> type) {
        this.type = type;

        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(ExcelColumn.class) != null) {
                ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);

                this.headerNames.add(field);
                this.columns.put(excelColumn.headerName(), field);
            }
        }
    }

    public T getInstance() {
        try {
            return this.type.getConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Field> getFieldByHeader(String headerName) {
        return Optional.ofNullable(this.columns.get(headerName));
    }
}
