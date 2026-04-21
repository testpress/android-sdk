package in.testpress.util;

import com.google.gson.Gson;

import org.greenrobot.greendao.converter.PropertyConverter;

public class ReflectionFormConverter implements PropertyConverter<ReflectionForm, String> {
    private static final Gson gson = new Gson();

    @Override
    public ReflectionForm convertToEntityProperty(String databaseValue) {
        if (databaseValue == null || databaseValue.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(databaseValue, ReflectionForm.class);
        } catch (Exception ignore) {
            return null;
        }
    }

    @Override
    public String convertToDatabaseValue(ReflectionForm entityProperty) {
        if (entityProperty == null) {
            return null;
        }
        try {
            return gson.toJson(entityProperty);
        } catch (Exception ignore) {
            return null;
        }
    }
}

