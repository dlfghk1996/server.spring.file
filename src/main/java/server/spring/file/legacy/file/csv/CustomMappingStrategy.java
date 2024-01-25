package server.spring.file.legacy.file.csv;

import com.opencsv.bean.BeanField;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

/** CSV 생성시 사용자가 정의한 열 이름과 순서를 모두 적용 하기위한 설정 */
public class CustomMappingStrategy<T> extends ColumnPositionMappingStrategy<T> {

	/** 1. 매핑 전략의 내용을 기반으로 하는 열 헤더 배열을 반환  */
	@Override
	public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {

		// 1. 부모 메소드를 호출 하지않으면 해당 필드의 value 값이 출력되지 않는다.
		super.generateHeader(bean);

		// 2. 엔티티의 필드에 정보가 담긴 맵의 사이즈를 구한다.
		final int numColumns = getFieldMap().values().size();

		// 3. 필드 이름을 담을 배열을 생성한다.
		String[] header = new String[numColumns];

		// 4. openCSV 에 필요한 기능을 포함하도록 java.lang.reflect.Field 클래스를 확장
		BeanField<T, Integer> beanField;

		// 5. 반복문을 돌려서 필드의 대한 정보를 가져온다.
		for (int i = 0; i < numColumns; i++) {
			 	// 지정된 열 위치에 대한 필드를 가져온다.
	            beanField = findField(i);
	    		// 바인딩 어노테이션이 존재하는 필드를 가져온다.
	            String columnHeaderName = extractHeaderName(beanField);
	            // 필드의 이름을 배열에 넣는다
	            header[i] = columnHeaderName;
	    }
		return header;
	}

	/** 2. @CsvBindByName 어노테이션의  column value를 가져온다. */
	private String extractHeaderName(BeanField<T, Integer> beanField) {
		if (beanField == null || beanField.getField() == null || beanField.getField().getDeclaredAnnotationsByType(CsvBindByName.class).length == 0) {
			return "";
	    }

		// @CsvBindByName은 여러개를 가질 수 있기 때문에, 특정인덱스의 @CsvBindByName를 지정한다.
        CsvBindByName bindByNameAnnotation = beanField.getField().getDeclaredAnnotationsByType(CsvBindByName.class)[0];
        return bindByNameAnnotation.column();
	}
}
