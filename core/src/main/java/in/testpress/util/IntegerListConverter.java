package in.testpress.util;

import android.text.TextUtils;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.Arrays;

public class IntegerListConverter implements PropertyConverter<IntegerList, String> {

    @Override
    public IntegerList convertToEntityProperty(String databaseValue) {
        if (databaseValue == null || databaseValue.isEmpty()) {
            return new IntegerList();
        }
        return new IntegerList(Arrays.asList(convertStringTOInt(databaseValue.split(","))));
    }

    @Override
    public String convertToDatabaseValue(IntegerList entityProperty) {
        return (entityProperty == null) ? "" : TextUtils.join(",", entityProperty);
    }

    private Integer[] convertStringTOInt(String[] string) {
        Integer number[] = new Integer[string.length];
        for (int i = 0; i < string.length; i++) {
            number[i] = Integer.parseInt(string[i]);
        }
        return number;
    }
}
