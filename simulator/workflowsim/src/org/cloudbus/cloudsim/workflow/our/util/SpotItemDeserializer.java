package org.cloudbus.cloudsim.workflow.our.util;

import com.google.gson.*;
import org.cloudbus.cloudsim.workflow.our.SpotPriceItem;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class SpotItemDeserializer implements JsonDeserializer<SpotPriceItem>{
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public SpotPriceItem deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        JsonObject jsonObject = (JsonObject)jsonElement;
        JsonElement element = jsonObject.get("timestamp");
        JsonElement element2 = jsonObject.get("spotPrice");

        GregorianCalendar date = null;

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date dateObj = format.parse(element.getAsString());
            date.setTime(dateObj);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
