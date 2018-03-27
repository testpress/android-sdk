package in.testpress.util;

import android.text.TextUtils;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.Arrays;

public class StringListConverter implements PropertyConverter<StringList, String> {

    @Override
    public StringList convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }
        if (databaseValue.isEmpty()) {
            return new StringList();
        }
        return new StringList(Arrays.asList(databaseValue.split(",")));
    }

    @Override
    public String convertToDatabaseValue(StringList entityProperty) {
        return (entityProperty == null) ? null : TextUtils.join(",", entityProperty);
    }

}
