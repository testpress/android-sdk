package in.testpress.util;

import android.text.TextUtils;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntegerListConverter implements PropertyConverter<List<Integer>, String> {

    @Override
    public List<Integer> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }
        if (databaseValue.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(convertStringTOInt(databaseValue.split(","))));
    }

    @Override
    public String convertToDatabaseValue(List<Integer> entityProperty) {
        return (entityProperty == null) ? null : TextUtils.join(",", entityProperty);
    }

    private Integer[] convertStringTOInt(String[] string) {
        Integer number[] = new Integer[string.length];
        for (int i = 0; i < string.length; i++) {
            number[i] = Integer.parseInt(string[i]);
        }
        return number;
    }
}
