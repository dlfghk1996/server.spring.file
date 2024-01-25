package server.spring.file.reader.util.workbook;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.web.multipart.MultipartFile;
import server.spring.file.reader.util.ExcelMetadata;

public class ExcelWorkbookReader {
    private ExcelMetadata excelMetadata;
    private String sheetName;

    public <T> ExcelWorkbookReader(Class<T> type) {
        this.excelMetadata = new ExcelMetadata<>(type);
    }

    public <T> ExcelWorkbookReader(Class<T> type, String sheetName) {
        this.excelMetadata = new ExcelMetadata<>(type);
        this.sheetName = sheetName;
    }

    public <T> List<T> readExcel(MultipartFile file, Class<T> type) {
        List<T> docs = new ArrayList<>();

        Sheet sheet = getSheet(file);
        List<String> headerNames = getHeaderNames(sheet.getRow(0));

        for (int rNum = 1; rNum < sheet.getPhysicalNumberOfRows(); rNum++) {
            Row row = sheet.getRow(rNum);
            try {
                T obj = type.getConstructor().newInstance();

                for (int cNum = 0; cNum < headerNames.size(); cNum++) {
                    Cell cell = row.getCell(cNum);
                    Optional<Field> field = this.excelMetadata.getFieldByHeader(headerNames.get(cNum));
                    if (field.isPresent()) {
                        Field f = field.get();
                        f.setAccessible(true);
                        setValueFromString(obj, f, getValue(cell).toString());
                    }
                }
                docs.add(obj);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return docs;
    }

    private Sheet getSheet(MultipartFile file) {
        Workbook workbook = getWorkbook(file);
        Sheet sheet = null;
        if (this.sheetName != null) {
            sheet = workbook.getSheet(this.sheetName);
        } else {
            sheet = workbook.getSheetAt(0);
        }
        if (sheet == null) throw new RuntimeException();
        return sheet;
    }

    private Workbook getWorkbook(MultipartFile file) {
        try {
            return XSSFWorkbookFactory.create(OPCPackage.open(file.getInputStream()));
        } catch (InvalidFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // data Type 이 다를 경우 Error
    private List<String> getHeaderNames(Row row) {
        List<String> headerNames = new ArrayList<>();
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            headerNames.add(getValue(row.getCell(i)));
        }
        return headerNames;
    }

    private <T> T getValue(Cell cell) {
        T value = null;
        NumberFormat f = NumberFormat.getInstance();
        f.setGroupingUsed(false);

        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    value = (T) cell.getStringCellValue();
                    break;
                case NUMERIC:
                    value = (T) (Integer) (((Double) cell.getNumericCellValue()).intValue());
                    break;
                case BLANK:
                    value = (T) "";
                    break;
                case FORMULA:
                    value = null;
                    break;
                case ERROR:
                    // value = cell.getErrorCellValue();
                    break;
            }
        }
        return value;
    }

    private <T> void setValueFromString(T obj, Field f, String cb) throws IllegalAccessException {
        if (f.getType().equals(int.class)) {
            f.set(obj, Integer.parseInt(cb));
        } else {
            f.set(obj, cb);
        }
    }

}
