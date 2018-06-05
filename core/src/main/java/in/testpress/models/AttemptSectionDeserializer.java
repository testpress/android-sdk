package in.testpress.models;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import in.testpress.models.greendao.AttemptSection;

/**
 * Map fields in info field to AttemptSection fields itself
 */

public class AttemptSectionDeserializer implements JsonDeserializer<AttemptSection> {

    @Override
    public AttemptSection deserialize(JsonElement json, Type typeOfT,
                                      JsonDeserializationContext context) throws JsonParseException {

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        AttemptSection attemptSection = gson.fromJson(json, AttemptSection.class);
        if (json.getAsJsonObject().has("info")) {
            JsonObject infoJsonObject = json.getAsJsonObject().getAsJsonObject("info");
            attemptSection.setName(infoJsonObject.get("name").getAsString());
            attemptSection.setDuration(infoJsonObject.get("duration").getAsString());
            attemptSection.setOrder(infoJsonObject.get("order").getAsInt());
        }
        return attemptSection;
    }

}
