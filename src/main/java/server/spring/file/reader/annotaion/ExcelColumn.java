package server.spring.file.reader.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/** 커스텀 어노테이션 **/

/**
 * @Retention(RetentionPolicy.RUNTIME) : 메모리에 적재
 * @Target : 어노테이션 사용을 허가하는 target을 설정
 **/

// FileDetail DTO 에서 엑셀에 표시하고 싶은 필드를 @ExcelHeaderColum 으로 표시한다.
// headerName이라는 메서드로 해당 필드의 헤더에 들어갈 이름을 정해서, 헤더셀에 삽입한다!

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {
    String headerName() default "";
}
