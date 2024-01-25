package server.spring.file.reader.util;

import java.util.List;
import java.util.Map;

public interface ExcelEventHandlerByRows {
    <T> void doNext(List<T> target, Map<T, String> errMap);
}
